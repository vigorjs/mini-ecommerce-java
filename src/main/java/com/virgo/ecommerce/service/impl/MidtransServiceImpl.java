package com.virgo.ecommerce.service.impl;

import com.virgo.ecommerce.service.MidtransService;
import com.virgo.ecommerce.utils.dto.restClientDto.MidtransRequestDTO;
import com.virgo.ecommerce.utils.dto.restClientDto.MidtransResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class MidtransServiceImpl implements MidtransService {

    private final RestClient restClient;
    private final HttpHeaders headers;

    @Value("${midtrans.api-url}")
    private String midtransApiUrl;

    public MidtransServiceImpl(RestClient restClient, HttpHeaders headers){
        this.restClient = restClient;
        this.headers = headers;
    }

    @Override
    public MidtransResponseDTO chargePayment(MidtransRequestDTO req) {
            var midtransResponseDto = restClient.post()
                    .uri(midtransApiUrl+"charge")
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .body(req)
                    .retrieve()
                    .body(MidtransResponseDTO.class);

            assert midtransResponseDto != null;
            if (midtransResponseDto.getStatus_code().equals("200") || midtransResponseDto.getStatus_code().equals("201")){
                return midtransResponseDto;
            }
            throw new IllegalArgumentException(midtransResponseDto.getStatus_message());
    }

    @Override
    public MidtransResponseDTO getStatus(String order_id) {
        try {
            var midtransResponseDto = restClient.get()
                    .uri(midtransApiUrl + order_id + "/status")
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .retrieve()
                    .body(MidtransResponseDTO.class);

            assert midtransResponseDto != null;
            return midtransResponseDto;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public MidtransResponseDTO changeStatus(String order_id, String status) {
        try {
            var midtransResponseDto = restClient.post()
                    .uri(midtransApiUrl + order_id + "/" + status)
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .retrieve()
                    .body(MidtransResponseDTO.class);

            assert midtransResponseDto != null;
            return midtransResponseDto;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
