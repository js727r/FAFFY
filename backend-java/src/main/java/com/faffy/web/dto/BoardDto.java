package com.faffy.web.dto;

import com.faffy.web.jpa.entity.Board;
import com.faffy.web.jpa.entity.User;
import com.faffy.web.jpa.type.BoardCategory;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;


@Data
public class BoardDto {
    private int no;
    private int writer_no;
    @NonNull
    private String title;
    private LocalDateTime datetime;
    @NonNull
    private String content;
    private User user;
    private BoardCategory category;

    private int hit;
    private int commentCount;

    private MultipartFile file;

    public BoardDto(){}

    @Builder
    public BoardDto(int no, String title, LocalDateTime datetime, String content, User user, BoardCategory category,
                    int hit, int commentCount, MultipartFile file) {
        if (StringUtils.hasLength(title))
        this.no = no;
        this.title = title;
        this.datetime = datetime;
        this.content = content;
        this.user = user;
        this.category = category;
        this.hit=hit;
        this.commentCount = commentCount;
        this.file = file;
    }

    public Board toEntityWriteBy(User user) {
        this.user = user;
        this.datetime = LocalDateTime.now();
        return toEntity();
    }
    public Board toEntity() {
        Board board = Board.builder()
                .title(title)
                .datetime(datetime)
                .content(content)
                .user(user)
                .category(category)
                .hit(hit)
                .commentCount(commentCount)
                .build();

        return board;
    }
}
