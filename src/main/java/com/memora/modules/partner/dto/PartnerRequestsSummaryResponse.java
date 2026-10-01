package com.memora.modules.partner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates incoming and outgoing pending partner requests.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartnerRequestsSummaryResponse {

    private List<PartnerRequestResponse> incoming = new ArrayList<>();
    private List<PartnerRequestResponse> outgoing = new ArrayList<>();

    public PartnerRequestsSummaryResponse() {
    }

    public PartnerRequestsSummaryResponse(List<PartnerRequestResponse> incoming, List<PartnerRequestResponse> outgoing) {
        this.incoming = incoming != null ? incoming : new ArrayList<>();
        this.outgoing = outgoing != null ? outgoing : new ArrayList<>();
    }

    public List<PartnerRequestResponse> getIncoming() {
        return incoming;
    }

    public void setIncoming(List<PartnerRequestResponse> incoming) {
        this.incoming = incoming != null ? incoming : new ArrayList<>();
    }

    public List<PartnerRequestResponse> getOutgoing() {
        return outgoing;
    }

    public void setOutgoing(List<PartnerRequestResponse> outgoing) {
        this.outgoing = outgoing != null ? outgoing : new ArrayList<>();
    }
}
