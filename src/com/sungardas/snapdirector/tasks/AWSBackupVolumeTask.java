package com.sungardas.snapdirector.tasks;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Volume;
import com.sungardas.snapdirector.aws.dynamodb.model.BackupEntry;
import com.sungardas.snapdirector.aws.dynamodb.model.BackupState;
import com.sungardas.snapdirector.aws.dynamodb.model.TaskEntry;
import com.sungardas.snapdirector.aws.dynamodb.model.WorkerConfiguration;
import com.sungardas.snapdirector.aws.dynamodb.repository.BackupRepository;
import com.sungardas.snapdirector.aws.dynamodb.repository.TaskRepository;
import com.sungardas.snapdirector.service.AWSCommunticationService;
import com.sungardas.snapdirector.service.ConfigurationService;
import com.sungardas.snapdirector.service.StorageService;
import com.sungardas.snapdirector.tasks.aws.VolumeBackup;
import com.sungardas.snapdirector.tasks.aws.sdfs.utils.SdfsManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

import static com.sungardas.snapdirector.aws.dynamodb.model.TaskEntry.TaskEntryStatus.RUNNING;
import static java.lang.String.format;

@Component
@Scope("prototype")
@Profile("prod")
public class AWSBackupVolumeTask implements BackupTask {
	private static final Logger LOG = LogManager.getLogger(AWSBackupVolumeTask.class);
	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private AWSCredentials amazonAWSCredentials;

	@Autowired
	AmazonEC2 ec2client;
	
	@Autowired
	StorageService storageService;

	@Autowired
	BackupRepository backupRepository;
	
	@Autowired
	private AWSCommunticationService awsCommunication;

	private TaskEntry taskEntry;

	@Autowired
	private ConfigurationService configurationService;

	private WorkerConfiguration configuration;

	public void setTaskEntry(TaskEntry taskEntry) {
		this.taskEntry = taskEntry;
	}

	public void execute() {
		String volumeId = taskEntry.getVolume();
		configuration = configurationService.getConfiguration();

		LOG.info(format("AWSBackupVolumeTask: Starting backup process for volume %s", volumeId));
		LOG.info("Task " + taskEntry.getId() + ": Change task state to 'inprogress'");
        taskEntry.setStatus(RUNNING.getStatus());
        taskRepository.save(taskEntry);
		
		SdfsManager sdfs = new SdfsManager(configuration);

		Volume tempVolume = null;
		String attachedDeviceName = null;
		
		tempVolume = VolumeBackup.createAndAttachBackupVolume(ec2client, volumeId, configuration.getConfigurationId());
		attachedDeviceName = detectFsDevName(tempVolume);

		String backupDate = String.valueOf(System.currentTimeMillis());
		String backupfileName = volumeId + "." + backupDate + ".backup";
		
		String snapshotId = tempVolume.getSnapshotId();
		String volumeType = tempVolume.getVolumeType();
		String iops = (tempVolume.getIops()!=null)?tempVolume.getIops().toString():"";
		String sizeGib = tempVolume.getSize().toString();
		
		BackupEntry backup = new BackupEntry(volumeId, backupfileName, backupDate, "", BackupState.INPROGRESS,
				configuration.getConfigurationId(),snapshotId,volumeType, iops,sizeGib);
		backupRepository.save(backup);

		boolean backupStatus = false;
		try {
			String source = null;
			if (configuration.isUseFakeBackup()) {
				source = configuration.getFakeBackupSource();
			} else {
				
				source = attachedDeviceName;
			}
			LOG.info("Starting copying: " + source + " to:" +backupfileName);
			storageService.copyFile(source, configuration.getSdfsMountPoint()+backupfileName);
			
			backupStatus = true;
		} catch (IOException e) {
			LOG.fatal(format("Backup of volume %s failed", volumeId));
			backup.setState(BackupState.FAILED.getState());
			backupRepository.save(backup);
		}

		if (backupStatus) {
			long backupSize = storageService.getSize(configuration.getSdfsMountPoint()+backupfileName);
			long backupCreationtime = storageService.getBackupCreationTime(configuration.getSdfsMountPoint()+backupfileName);
			LOG.info("Backup creation time: " + backupCreationtime);
			LOG.info("Backup size: " + backupSize);

			LOG.info("Put backup entry to the Backup List: " + backup.toString());
			backup.setState(BackupState.COMPLETED.getState());
			backup.setSize(String.valueOf(backupSize));
			backupRepository.save(backup);
		}

		LOG.info("Detaching volume" + tempVolume.getVolumeId());
		awsCommunication.detachVolume(tempVolume);
		LOG.info("Deleting temporary volume" + tempVolume.getVolumeId());
		awsCommunication.deleteVolume(tempVolume);

		LOG.info(format("Backup process for volume %s finished successfully ", volumeId));
		LOG.info("Task " + taskEntry.getId() + ": Delete completed task:" + taskEntry.getId());
        taskRepository.delete(taskEntry);
        LOG.info("Task completed.");
	}
	
	private String detectFsDevName(Volume volume) {
		String devname = volume.getAttachments().get(0).getDevice();
		File volf = new File(devname);
		if (!volf.exists() || !volf.isFile()) {
			LOG.info(format("Cant find attached source: %s", volume));
			
			devname = "/dev/xvd" + devname.substring(devname.length()-1);
			LOG.info(format("New sourcepash : %s", devname));
		}
		return devname;
	}

}
