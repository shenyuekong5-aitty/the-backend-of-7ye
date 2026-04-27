package org.example.springboot2.book.controller;

import org.example.springboot2.book.entity.Book;
import org.example.springboot2.book.service.BookService;
import org.example.springboot2.user.entity.User;
import org.example.springboot2.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/book")
public class BookController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getBookList() {
        List<Book> books = bookService.getAllBooks();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("items", books);
        data.put("message", "获取书籍列表成功");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addBook(@RequestHeader(value = "token", required = false) String token,
                                                       @RequestBody Book book) {
        // 1. 校验 token 是否存在
        if (token == null || token.isEmpty()) {
            return buildMissingTokenResponse();
        }

        // 2. 校验管理员权限
        if (!isAdmin(token)) {
            return buildUnauthorizedResponse();
        }

        // 3. 执行业务逻辑
        try {
            Book saved = bookService.addBook(book);
            return ResponseEntity.ok(successResponse(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse("新增书籍失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateBook(@RequestHeader(value = "token", required = false) String token,
                                                          @RequestBody Book book) {
        if (token == null || token.isEmpty()) {
            return buildMissingTokenResponse();
        }
        if (!isAdmin(token)) {
            return buildUnauthorizedResponse();
        }
        try {
            Book updated = bookService.updateBook(book);
            return ResponseEntity.ok(successResponse(updated));
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse("编辑书籍失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteBook(@RequestHeader(value = "token", required = false) String token,
                                                          @PathVariable Long id) {
        if (token == null || token.isEmpty()) {
            return buildMissingTokenResponse();
        }
        if (!isAdmin(token)) {
            return buildUnauthorizedResponse();
        }
        try {
            bookService.deleteBook(id);
            return ResponseEntity.ok(successResponse(null));
        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse("删除书籍失败: " + e.getMessage());
        }
    }

    private boolean isAdmin(String token) {
        User user = userService.getUserByToken(token);
        return user != null && user.getRoles() != null && user.getRoles().contains("admin");
    }

    private ResponseEntity<Map<String, Object>> buildMissingTokenResponse() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 401);
        resp.put("message", "缺少认证 Token");
        resp.put("data", null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
    }

    private ResponseEntity<Map<String, Object>> buildUnauthorizedResponse() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 403);
        resp.put("message", "无权限执行此操作");
        resp.put("data", null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
    }

    private Map<String, Object> successResponse(Object data) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("message", "操作成功");
        resp.put("data", data);
        return resp;
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 500);
        resp.put("message", message);
        resp.put("data", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
    }
}