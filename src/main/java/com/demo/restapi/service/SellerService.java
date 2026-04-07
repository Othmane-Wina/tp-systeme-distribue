package com.demo.restapi.service;

import com.demo.restapi.dto.SellerFullDTO;
import com.demo.restapi.dto.SellerPreviewDTO;
import com.demo.restapi.entity.Seller;
import com.demo.restapi.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import com.demo.restapi.mapper.DTOMapper;
import org.springframework.stereotype.Service;
import com.demo.restapi.repository.SellerRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepository sellerRepository;
    private final DTOMapper mapper;

    public List<SellerPreviewDTO> getAllSellers() {
        return sellerRepository.findAll().stream()
                .map(mapper::toSellerPreview)
                .collect(Collectors.toList());
    }

    public SellerFullDTO getSellerById(String id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller " + id + " not found"));
        return mapper.toSellerFull(seller);
    }

    public SellerFullDTO createSeller(SellerFullDTO dto) {
        Seller seller = mapper.toSellerEntity(dto);

        seller.setRating(0.0); // New sellers start with 0 rating
        // createdAt is handled by the entity default value LocalDateTime.now()

        return mapper.toSellerFull(sellerRepository.save(seller));
    }

    // Requirement: Update (email forbidden)
    public SellerFullDTO updateSeller(String id, SellerFullDTO updateDTO) {
        Seller existing = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller " + id + " not found"));

        existing.setStoreName(updateDTO.getStoreName());
        existing.setPhone(updateDTO.getPhone());
        existing.setAddress(updateDTO.getAddress() != null ? /* map address */ null : existing.getAddress());
        // We do NOT call existing.setEmail() here.

        return mapper.toSellerFull(sellerRepository.save(existing));
    }

    public void deleteSeller(String id) {
        if (!sellerRepository.existsById(id))
            throw new ResourceNotFoundException("Seller " + id + " not found");
        sellerRepository.deleteById(id);
    }
}
