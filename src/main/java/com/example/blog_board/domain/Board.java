package com.example.blog_board.domain;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Board {
    private Long boardId;
    private String title;
    private String content;
    private LocalDateTime createDate;
    private Integer read;
    private String name;

    private Long memberId;


    public Board(String title, String content, String name) {
        this.title = title;
        this.content = content;
        this.name = name;
    }

}

