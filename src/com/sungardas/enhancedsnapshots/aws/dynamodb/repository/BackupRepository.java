package com.sungardas.enhancedsnapshots.aws.dynamodb.repository;

import java.util.List;

import com.sungardas.enhancedsnapshots.aws.dynamodb.model.BackupEntry;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.springframework.data.repository.PagingAndSortingRepository;

@EnableScan
@EnableScanCount
public interface BackupRepository extends PagingAndSortingRepository<BackupEntry, String> {
    List<BackupEntry> findByVolumeId(String volumeId);

    List<BackupEntry> findByFileName(String fileName);

    List<BackupEntry> findAll();
}
