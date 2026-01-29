package com.caro.bizkit.domain.withdrawl.service;

import com.caro.bizkit.domain.withdrawl.dto.WithdrawalResponse;
import com.caro.bizkit.domain.withdrawl.repository.WithdrawalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final WithdrawalRepository withdrawalRepository;

    @Transactional(readOnly = true)
    public List<WithdrawalResponse> getAllWithdrawals() {
        return StreamSupport.stream(withdrawalRepository.findAll().spliterator(), false)
                .map(WithdrawalResponse::from)
                .toList();
    }
}
