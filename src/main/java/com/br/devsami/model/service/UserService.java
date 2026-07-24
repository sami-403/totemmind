package com.br.devsami.model.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.br.devsami.model.entity.User;
import com.br.devsami.model.repository.UserRepository;
import com.br.devsami.util.CpfValidator;

public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public List<User> listUsers(int page, int pageSize) {
        int pagesCount = countPages(pageSize);
        int validatedPage = page < 1 ? 0 : page;

        if(page > pagesCount){
            validatedPage = pagesCount;
        }

        return userRepository.findAll(pageSize, validatedPage);
    }

    public List<User> listUsers(int page, int pageSize, boolean reverse) {
        int pagesCount = countPages(pageSize);
        int validatedPage = page < 1 ? 0 : page;

        if(page > pagesCount){
            validatedPage = pagesCount;
        }

        return userRepository.findAllSorted(pageSize, validatedPage, reverse);
    }

    public int countPages(int pageSize) {
        Long entriesCount = userRepository.countEntries();

        return (int) Math.ceil((double) entriesCount / pageSize);
    }

    // 1. Buscar usuário
    public Optional<User> findByCpf(String cpf) {
        if (cpf.isEmpty() || CpfValidator.isValidCpf(cpf)){
            throw new IllegalArgumentException(CpfValidator.validate(cpf));
        }

        return userRepository.findByCpf(cpf);
    }

    // 2. Criar usuário
    public User createUser(String name, String cpf, LocalDate birthDate) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome obrigatório");
        }

        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF obrigatório");
        }

        String validationError = CpfValidator.validate(cpf);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        if (birthDate == null) {
            throw new IllegalArgumentException("Data de nascimento obrigatória");
        }

        if (userRepository.existsByCpf(cpf)) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }

        var user = new User();
        user.setName(name);
        user.setCpf(cpf);
        user.setBirthDate(birthDate);

        userRepository.save(user);

        return user;
    }

    // 3. Fluxo principal do totem (ESSA É A PARTE MAIS IMPORTANTE)
    public User findOrCreateUser(String name, String cpf, LocalDate birthDate) {

        Optional<User> existing = userRepository.findByCpf(cpf);

        return existing.orElseGet(() -> createUser(name, cpf, birthDate));

    }
}