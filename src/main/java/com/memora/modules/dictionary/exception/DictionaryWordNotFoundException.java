package com.memora.modules.dictionary.exception;

import com.memora.common.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when the requested dictionary word is not found in the external dictionary service.
 */
public class DictionaryWordNotFoundException extends ApiException {

    public DictionaryWordNotFoundException(String word) {
        super("Word not found. Try checking the spelling.", HttpStatus.NOT_FOUND);
    }
}
