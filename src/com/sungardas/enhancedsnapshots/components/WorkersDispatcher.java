package com.sungardas.enhancedsnapshots.components;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.amazonaws.AmazonClientException;
import com.sungardas.enhancedsnapshots.aws.dynamodb.model.TaskEntry;
import com.sungardas.enhancedsnapshots.aws.dynamodb.repository.TaskRepository;
import com.sungardas.enhancedsnapshots.exception.EnhancedSnapshotsInterruptedException;
import com.sungardas.enhancedsnapshots.service.NotificationService;
import com.sungardas.enhancedsnapshots.service.SDFSStateService;
import com.sungardas.enhancedsnapshots.service.TaskService;
import com.sungardas.enhancedsnapshots.tasks.executors.TaskExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import static com.sungardas.enhancedsnapshots.aws.dynamodb.model.TaskEntry.TaskEntryStatus.ERROR;
import static com.sungardas.enhancedsnapshots.aws.dynamodb.model.TaskEntry.TaskEntryStatus.RUNNING;

@Service
@DependsOn("SystemService")
public class WorkersDispatcher {

    private static final Comparator<TaskEntry> taskComparatorByTimeAndPriority = (o1, o2) -> {
        int priority = o2.getPriority() - o1.getPriority();
        if (priority != 0) {
            return priority;
        }
        return o1.getSchedulerTime().compareTo(o2.getSchedulerTime());
    };

    @Autowired
    private ConfigurationMediator configurationMediator;
    @Autowired
    @Qualifier("awsBackupVolumeTaskExecutor")
    private TaskExecutor awsBackupVolumeTaskExecutor;
    @Autowired
    @Qualifier("awsDeleteTaskExecutor")
    private TaskExecutor awsDeleteTaskExecutor;
    @Autowired
    @Qualifier("awsRestoreVolumeTaskExecutor")
    private TaskExecutor awsRestoreVolumeTaskExecutor;
    @Autowired
    private TaskService taskService;
    @Autowired
    private SDFSStateService sdfsStateService;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private NotificationService notificationService;

    private ExecutorService executor;

    @PostConstruct
    private void init() {
        executor = Executors.newSingleThreadExecutor();
        executor.execute(new TaskWorker());
    }

    @PreDestroy
    public void destroy() {
        executor.shutdownNow();
    }

    private Set<TaskEntry> sortByTimeAndPriority(List<TaskEntry> list) {
        Set<TaskEntry> result = new TreeSet<>(taskComparatorByTimeAndPriority);

        result.addAll(list);

        return result;
    }

    private class TaskWorker implements Runnable {
        private final Logger LOGtw = LogManager.getLogger(TaskWorker.class);

        @Override
        public void run() {
            String instanceId = configurationMediator.getConfigurationId();

            LOGtw.info("Starting worker dispatcher");
            while (true) {
                if (Thread.interrupted()) {
                    throw new EnhancedSnapshotsInterruptedException("Task interrupted");
                }
                TaskEntry entry = null;
                try {
                    Set<TaskEntry> taskEntrySet = sortByTimeAndPriority(taskRepository.findByStatusAndInstanceIdAndRegular(TaskEntry.TaskEntryStatus.QUEUED.getStatus(), instanceId, Boolean.FALSE.toString()));
                    while (!taskEntrySet.isEmpty()) {
                        entry = taskEntrySet.iterator().next();

                        if (!taskService.isCanceled(entry.getId())) {
                            switch (TaskEntry.TaskEntryType.getType(entry.getType())) {
                                case BACKUP:
                                    //TODO remove when sdfscli --expand volume will be fixed
                                    if (!sdfsStateService.sdfsIsAvailable()) {
                                        break;
                                    }
                                    LOGtw.info("Task was identified as backup");
                                    awsBackupVolumeTaskExecutor.execute(entry);
                                    break;
                                case DELETE: {
                                    LOGtw.info("Task was identified as delete backup");
                                    awsDeleteTaskExecutor.execute(entry);
                                    break;
                                }
                                case RESTORE:
                                    if (!sdfsStateService.sdfsIsAvailable()) {
                                        //TODO remove when sdfscli --expand volume will be fixed
                                        break;
                                    }
                                    LOGtw.info("Task was identified as restore");
                                    awsRestoreVolumeTaskExecutor.execute(entry);
                                    break;
                                case SYSTEM_BACKUP: {
                                    LOGtw.info("Task was identified as system backup");
                                    notificationService.notifyAboutTaskProgress(entry.getId(), "System backup started", 0);
                                    entry.setStatus(RUNNING.getStatus());
                                    taskRepository.save(entry);
                                    sdfsStateService.backupState(entry.getId());
                                    taskRepository.delete(entry);
                                    notificationService.notifyAboutTaskProgress(entry.getId(), "System backup finished", 100);
                                    break;
                                }
                                case UNKNOWN:
                                    LOGtw.warn("Executor for type {} is not implemented. Task {} is going to be removed.", entry.getType(), entry.getId());
                                    taskService.removeTask(entry.getId());
                            }
                        } else {
                            LOGtw.debug("Task canceled: {}", entry);
                        }
                        taskEntrySet = sortByTimeAndPriority(taskRepository.findByStatusAndInstanceIdAndRegular(TaskEntry.TaskEntryStatus.QUEUED.getStatus(), instanceId, Boolean.FALSE.toString()));
                    }
                    sleep();
                } catch (AmazonClientException e) {
                    LOGtw.error(e);
                } catch (EnhancedSnapshotsInterruptedException e) {
                    return;
                    // this is required to close thread when uninstalling system
                } catch (IllegalStateException e) {
                    LOGtw.warn("Stopping worker dispatcher ...");
                    return;
                } catch (Exception e) {
                    LOGtw.error(e);
                    if (entry != null) {
                        entry.setStatus(ERROR.getStatus());
                        taskRepository.save(entry);
                    }
                }
            }
        }

        private void sleep() {
            try {
                TimeUnit.MILLISECONDS.sleep(configurationMediator.getWorkerDispatcherPollingRate());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
