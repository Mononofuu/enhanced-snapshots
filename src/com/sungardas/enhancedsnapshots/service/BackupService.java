package com.sungardas.enhancedsnapshots.service;

import com.sungardas.enhancedsnapshots.aws.dynamodb.model.BackupEntry;

import java.util.List;

import java.util.Collection;

public interface BackupService {
    void deleteBackup(String backupName, String user);

    List<BackupEntry> getBackupList(String volumeId);

    void deleteBackup(Collection<BackupEntry> backupEntries, String user);
}
