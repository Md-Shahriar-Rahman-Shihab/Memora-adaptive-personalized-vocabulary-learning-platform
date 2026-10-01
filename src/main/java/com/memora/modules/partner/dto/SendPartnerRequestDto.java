package com.memora.modules.partner.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request payload for sending a learning partner connection request.
 */
public class SendPartnerRequestDto {

    @NotNull(message = "Target user ID cannot be null")
    @Positive(message = "Target user ID must be a positive number")
    private Long targetUserId;

    public SendPartnerRequestDto() {
    }

    public SendPartnerRequestDto(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }
}
