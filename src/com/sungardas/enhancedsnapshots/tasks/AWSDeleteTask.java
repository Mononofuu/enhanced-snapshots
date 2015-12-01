package com.sungardas.enhancedsnapshots.tasks;

import com.sungardas.enhancedsnapshots.aws.dynamodb.model.BackupEntry;
import com.sungardas.enhancedsnapshots.aws.dynamodb.model.TaskEntry;
import com.sungardas.enhancedsnapshots.aws.dynamodb.repository.BackupRepository;
import com.sungardas.enhancedsnapshots.aws.dynamodb.repository.TaskRepository;
import com.sungardas.enhancedsnapshots.dto.ExceptionDto;
import com.sungardas.enhancedsnapshots.exception.EnhancedSnapshotsException;
import com.sungardas.enhancedsnapshots.service.NotificationService;
import com.sungardas.enhancedsnapshots.service.StorageService;
import com.sungardas.enhancedsnapshots.service.TaskService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.sungardas.enhancedsnapshots.aws.dynamodb.model.TaskEntry.TaskEntryStatus.ERROR;
import static com.sungardas.enhancedsnapshots.aws.dynamodb.model.TaskEntry.TaskEntryStatus.RUNNING;

@Component
@Scope("prototype")
@Profile("prod")
public class AWSDeleteTask implements DeleteTask {

    private static final Logger LOG = LogManager.getLogger(DeleteFakeTask.class);

    private TaskEntry taskEntry;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private BackupRepository backupRepository;

    @Autowired
    private StorageService storageService;

    @Autowired
    private TaskService taskService;

    @Override
    public void setTaskEntry(TaskEntry taskEntry) {
        this.taskEntry = taskEntry;
    }

    @Override
    public void execute() {
        LOG.info("Task " + taskEntry.getId() + ": Change task state to 'running'");
        notificationService.notifyAboutTaskProgress(taskEntry.getId(), "Starting delete task", 0);

        taskEntry.setStatus(RUNNING.getStatus());
        taskRepository.save(taskEntry);

        notificationService.notifyAboutTaskProgress(taskEntry.getId(), "Finding source file", 30);
        BackupEntry backupEntry = new BackupEntry();
        backupEntry.setVolumeId(taskEntry.getVolume());
        backupEntry.setFileName(taskEntry.getSourceFileName());

        try {
            notificationService.notifyAboutTaskProgress(taskEntry.getId(), "Deleting file...", 60);
            storageService.deleteFile(backupEntry.getFileName());
            backupRepository.delete(backupEntry);
            taskService.complete(taskEntry);
            LOG.info("Task " + taskEntry.getId() + ": Change task state to 'complete'");
            notificationService.notifyAboutTaskProgress(taskEntry.getId(), "Task complete", 100);
        } catch (EnhancedSnapshotsException e){
            LOG.error(e);
            notificationService.notifyAboutError(new ExceptionDto("Delete task has failed", e.getLocalizedMessage()));
            taskEntry.setStatus(ERROR.getStatus());
            taskRepository.save(taskEntry);
        }
    }
}
