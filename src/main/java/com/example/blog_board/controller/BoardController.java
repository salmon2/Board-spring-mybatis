package com.example.blog_board.controller;

import com.example.blog_board.domain.Board;
import com.example.blog_board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/hello")
    public String Hello(){
        return "/board/hello";
    }

    @GetMapping("/test")
    public String test(Model model){
        model.addAttribute("cnt", boardService.boardCount());
        model.addAttribute("test", boardService.boardList());

        return "/board/hello";
    }

    @GetMapping
    public String main(Model model){
        model.addAttribute("boards", boardService.boardList());

        return "/board/boards";
    }

    @GetMapping("/{boardId}")
    public String board(@PathVariable long boardId, Model model){
        model.addAttribute("board", boardService.findById(boardId));

        return "/board/board";
    }
    @GetMapping("/add")
    public String add(){
        return "/board/addForm";
    }

    @PostMapping("/add")
    public String add(@RequestParam String title, @RequestParam String content,
                      @RequestParam String name, RedirectAttributes redirectAttributes){
        Board newBoard = new Board(title, content, name);
        Long boardId = boardService.add(newBoard);
        System.out.println("boardId = " + boardId);

        redirectAttributes.addAttribute("boardId", boardId);
        redirectAttributes.addAttribute("status", true);

        return "redirect:/boards/{boardId}";
    }

    @GetMapping("/{boardId}/edit")
    public String editForm(@PathVariable Long boardId, Model model){
        Board findBoard = boardService.findById(boardId);
        model.addAttribute("board", findBoard);

        return "board/editForm";
    }

    @PostMapping("/{boardId}/edit")
    public String editForm(@PathVariable Long boardId, @RequestParam String title,
                           @RequestParam String content, @RequestParam String name)
    {

        Board findBoard = boardService.findById(boardId);
        findBoard.setTitle(title);
        findBoard.setContent(content);
        findBoard.setName(name);

        boardService.update(findBoard);

        return "redirect:/boards/{boardId}";
    }

    @GetMapping("/{boardId}/delete")
    public String deleteBoard(@PathVariable Long boardId){
        boardService.deleteById(boardId);
        return "redirect:/boards";
    }




}
