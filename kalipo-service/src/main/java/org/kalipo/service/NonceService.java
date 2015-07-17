package org.kalipo.service;

import org.joda.time.DateTime;
import org.kalipo.domain.Nonce;
import org.kalipo.repository.NonceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Service for render plaintext to HTML.
 */
@Service
public class NonceService {

    private final Logger log = LoggerFactory.getLogger(NonceService.class);

    @Inject
    private NonceRepository nonceRepository;

    public Nonce createNonce() {

        Nonce nonce = new Nonce();
        nonce.setValidUntil(DateTime.now().plusDays(1));

        return nonceRepository.save(nonce);
    }

    // todo delete expired nonces

}
