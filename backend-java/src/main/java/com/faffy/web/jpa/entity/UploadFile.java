package com.faffy.web.jpa.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public class UploadFile extends BaseEntity {
    @Column(name = "file_name")
    private String fileName;

    @Column(name = "upload_path")
    private String uploadPath;

    private String uuid;
}
