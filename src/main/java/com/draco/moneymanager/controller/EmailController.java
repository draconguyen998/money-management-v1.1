package com.draco.moneymanager.controller;

import com.draco.moneymanager.entity.ProfileEntity;
import com.draco.moneymanager.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {
    private final ExcelService excelService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;
    private final EmailService emailService;

    // 1) DOWNLOAD excel (frontend bấm Download)
    @GetMapping("/income-excel")
    public ResponseEntity<byte[]> downloadIncomeExcel() throws IOException {
        ProfileEntity profile = profileService.getCurrentProfile();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        excelService.writeIncomesToExcel(
                baos,
                incomeService.getCurrentMonthIncomeForCurrentUser()
        );

        byte[] fileBytes = baos.toByteArray();
        String filename = "income_details_" + LocalDate.now() + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentLength(fileBytes.length)
                .body(fileBytes);
    }

    // 2) EMAIL excel (frontend bấm Email)
    @PostMapping("/income-excel")
    public ResponseEntity<?> emailIncomeExcel() throws IOException {
        ProfileEntity profile = profileService.getCurrentProfile();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        excelService.writeIncomesToExcel(
                baos,
                incomeService.getCurrentMonthIncomeForCurrentUser()
        );

        byte[] fileBytes = baos.toByteArray();
        String filename = "income_details_" + LocalDate.now() + ".xlsx";

        String to = profile.getEmail(); // hoặc profile.getUser().getEmail() tùy entity bạn
        String subject = "Your Income Report";
        String body = "Hi " + profile.getFullName() + ",\n\nAttached is your income report.\n\nRegards,\nMoney Manager";

        emailService.sendEmailWithAttachment(to, subject, body, fileBytes, filename);

        return ResponseEntity.ok().body(
                java.util.Map.of("message", "Income details emailed successfully")
        );
    }

}
