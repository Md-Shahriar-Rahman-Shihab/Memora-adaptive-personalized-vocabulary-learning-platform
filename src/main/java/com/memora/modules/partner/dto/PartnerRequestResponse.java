package com.memora.modules.partner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Detailed representation of a partner connection request.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartnerRequestResponse {

    private Long id;
    private PartnerUserSummaryResponse sender;
    private PartnerUserSummaryResponse receiver;
    private String status;
    private Instant createdAt;
    private boolean incoming;

    public PartnerRequestResponse() {
    }

    public PartnerRequestResponse(Long id, PartnerUserSummaryResponse sender, PartnerUserSummaryResponse receiver,
                                  String status, Instant createdAt, boolean incoming) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.status = status;
        this.createdAt = createdAt;
        this.incoming = incoming;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PartnerUserSummaryResponse getSender() {
        return sender;
    }

    public void setSender(PartnerUserSummaryResponse sender) {
        this.sender = sender;
    }

    public PartnerUserSummaryResponse getReceiver() {
        return receiver;
    }

    public void setReceiver(PartnerUserSummaryResponse receiver) {
        this.receiver = receiver;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public void setIncoming(boolean incoming) {
        this.incoming = incoming;
    }
}
