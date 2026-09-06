package com.laptophub.storage.job;

import com.laptophub.shared.properties.MinioProperties;
import com.laptophub.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OrphanedObjectCleanupJob {

    private final StorageService storageService;
    private final MinioProperties properties;

    public OrphanedObjectCleanupJob(
            StorageService storageService,
            MinioProperties properties
    ) {
        this.storageService = storageService;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${storage.minio.cleanup.interval}")
    public void cleanup() {

        if (!properties.cleanup().enabled()) {
            return;
        }

        log.info("Bắt đầu cleanup orphaned objects");

        cleanupPrefix("brands/tmp/");
        cleanupPrefix("products/tmp/");

        log.info("Hoàn thành cleanup orphaned objects");
    }

    private void cleanupPrefix(String prefix) {

        List<String> expiredKeys =
                storageService.listObjectsOlderThan(
                        prefix,
                        properties.cleanup().orphanAge()
                );

        for (String objectKey : expiredKeys) {

            try {

                storageService.delete(objectKey);

                log.info(
                        "Đã xóa orphaned object: {}",
                        objectKey
                );

            } catch (Exception e) {

                log.error(
                        "Không thể xóa orphaned object: {}",
                        objectKey,
                        e
                );
            }
        }
    }
}