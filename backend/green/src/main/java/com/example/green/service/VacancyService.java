package com.example.green.service;

import com.example.green.api.dto.request.VacancyRequestDto;
import com.example.green.api.dto.response.VacancyResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.VacancyMapper;
import com.example.green.domain.entity.User;
import com.example.green.domain.entity.Vacancy;
import com.example.green.domain.repository.UserRepository;
import com.example.green.domain.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VacancyService {
    private final VacancyRepository vacancyRepository;
    private final UserRepository userRepository;
    private final VacancyMapper vacancyMapper;

    public List<VacancyResponseDto> findAll() {
        return vacancyRepository.findAll().stream()
                .map(vacancyMapper::toDto)
                .toList();
    }

    public VacancyResponseDto findById(Long id) {
        return vacancyMapper.toDto(getVacancyOrThrow(id));
    }

    public VacancyResponseDto create(VacancyRequestDto request) {
        User hrManager = getUserOrThrow(request.getHrManagerId());
        Vacancy saved = vacancyRepository.save(vacancyMapper.toEntity(request, hrManager));
        return vacancyMapper.toDto(saved);
    }

    public VacancyResponseDto update(Long id, VacancyRequestDto request) {
        Vacancy entity = getVacancyOrThrow(id);
        User hrManager = getUserOrThrow(request.getHrManagerId());
        vacancyMapper.updateEntity(entity, request, hrManager);
        Vacancy saved = vacancyRepository.save(entity);
        return vacancyMapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!vacancyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vacancy not found: id=" + id);
        }
        vacancyRepository.deleteById(id);
    }

    private Vacancy getVacancyOrThrow(Long id) {
        return vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found: id=" + id));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + id));
    }
}
