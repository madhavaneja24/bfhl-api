package com.bfhl.service;

import com.bfhl.dto.BfhlRequest;
import com.bfhl.dto.BfhlResponse;

/**
 * Service interface for BFHL processing logic.
 * Defines the contract for processing input data arrays.
 */
public interface BfhlService {

    /**
     * Processes the input data array and returns a structured response.
     *
     * @param request the incoming request containing the data array
     * @return BfhlResponse with categorized data, sum, and concat string
     */
    BfhlResponse processData(BfhlRequest request);
}
