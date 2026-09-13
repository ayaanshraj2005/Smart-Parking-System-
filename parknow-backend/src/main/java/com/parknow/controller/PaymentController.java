package com.parknow.controller;

import com.parknow.dto.response.ApiResponse;
import com.parknow.entity.Payment;
import com.parknow.repository.PaymentRepository;
import com.parknow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Payment>>> getMyPayments(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<Payment> payments = paymentRepository.findByUserIdOrderByPaymentTimeDesc(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("User payments fetched successfully", payments));
    }
}
