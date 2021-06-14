package com.example.blog_board.service;

import com.example.blog_board.domain.Board;
import com.example.blog_board.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    private final BoardMapper boardMapper;

    public int boardCount(){
        return boardMapper.boardCount();
    }
    public List<Board> boardList(){
        return boardMapper.findAll();
    }

    public Board findById(Long boardId){
        return boardMapper.findById(boardId);
    }

    @Transactional
    public Long add(Board board) {
        boardMapper.save(board);
        return board.getBoardId();
    }

    @Transactional
    public Long update(Board board){
        return boardMapper.update(board);
    }

    public void deleteById(Long boardId) {
        boardMapper.delete(boardId);
    }
}
