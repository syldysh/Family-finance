package com.familyfinance.finance.service;

import com.familyfinance.common.exception.NotFoundException;
import com.familyfinance.finance.dto.CreateTransactionRequest;
import com.familyfinance.finance.dto.TransactionResponse;
import com.familyfinance.finance.entity.Category;
import com.familyfinance.finance.entity.Family;
import com.familyfinance.finance.entity.OperationType;
import com.familyfinance.finance.entity.Transaction;
import com.familyfinance.finance.repository.FamilyMemberRepository;
import com.familyfinance.finance.repository.FamilyRepository;
import com.familyfinance.finance.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private static final LocalDate DATE_MIN = LocalDate.of(1900, 1, 1);
    private static final LocalDate DATE_MAX = LocalDate.of(2999, 12, 31);

    private final TransactionRepository transactionRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final CategoryService categoryService;
    private final AccessService accessService;
    private final NotificationService notificationService;

    public TransactionService(TransactionRepository transactionRepository,
                              FamilyRepository familyRepository,
                              FamilyMemberRepository familyMemberRepository,
                              CategoryService categoryService,
                              AccessService accessService,
                              NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.familyRepository = familyRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.categoryService = categoryService;
        this.accessService = accessService;
        this.notificationService = notificationService;
    }

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request, Long userId) {
        log.info("Создание транзакции: userId={}, type={}, amount={}, familyId={}",
                userId, request.getType(), request.getAmount(), request.getFamilyId());

        Category category = categoryService.findById(request.getCategoryId());

        Family family = null;
        if (request.getFamilyId() != null) {
            accessService.checkFamilyMember(request.getFamilyId(), userId);
            family = familyRepository.findById(request.getFamilyId())
                    .orElseThrow(() -> new NotFoundException("Группа не найдена"));
        }

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getDate());
        transaction.setUserId(userId);
        transaction.setCategory(category);
        transaction.setFamily(family);
        transactionRepository.save(transaction);
        log.info("Транзакция создана: id={}, userId={}, familyId={}",
                transaction.getId(), userId, family != null ? family.getId() : null);

        if (family != null) {
            notificationService.notifyFamilyUpdate(family.getId());
        }

        return toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getPersonalTransactions(Long userId, LocalDate from, LocalDate to,
                                                              OperationType type, Long categoryId) {
        List<Transaction> transactions;
        if (type == null && categoryId == null) {
            transactions = transactionRepository.findPersonalByDate(userId, orMin(from), orMax(to));
        } else {
            transactions = transactionRepository.findPersonal(userId, from, to, type, categoryId);
        }
        return toResponseList(transactions);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getFamilyTransactions(Long familyId, Long userId,
                                                            LocalDate from, LocalDate to,
                                                            OperationType type, Long categoryId,
                                                            Long filterUserId) {
        accessService.checkFamilyMember(familyId, userId);
        List<Transaction> transactions;
        if (type == null && categoryId == null) {
            transactions = transactionRepository.findByFamilyByDate(familyId, orMin(from), orMax(to), filterUserId);
        } else {
            transactions = transactionRepository.findByFamily(familyId, from, to, type, categoryId, filterUserId);
        }
        return toResponseList(transactions);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllAccessibleTransactions(Long userId, List<Long> familyIds,
                                                                   LocalDate from, LocalDate to) {
        List<Transaction> transactions = transactionRepository
                .findAllAccessibleSafe(userId, familyIds, orMin(from), orMax(to));
        return toResponseList(transactions);
    }

    private LocalDate orMin(LocalDate d) {
        return d != null ? d : DATE_MIN;
    }

    private LocalDate orMax(LocalDate d) {
        return d != null ? d : DATE_MAX;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllForUser(Long userId, LocalDate from, LocalDate to) {
        List<Long> familyIds = familyRepository.findAllByUserId(userId).stream()
                .map(f -> f.getId()).toList();
        return getAllAccessibleTransactions(userId, familyIds, from, to);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Операция не найдена"));

        if (transaction.getFamily() != null) {
            accessService.checkFamilyMember(transaction.getFamily().getId(), userId);
        } else if (!transaction.getUserId().equals(userId)) {
            throw new com.familyfinance.common.exception.AccessDeniedException("Нет доступа к этой операции");
        }

        return toResponse(transaction);
    }

    private List<TransactionResponse> toResponseList(List<Transaction> transactions) {
        List<Long> familyIds = transactions.stream()
                .map(t -> t.getFamily() != null ? t.getFamily().getId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, String> emailLookup = Map.of();
        if (!familyIds.isEmpty()) {
            emailLookup = familyMemberRepository.findAllByFamilyIds(familyIds).stream()
                    .collect(Collectors.toMap(
                            m -> m.getFamily().getId() + ":" + m.getUserId(),
                            m -> m.getUserEmail(),
                            (a, b) -> a
                    ));
        }

        Map<String, String> lookup = emailLookup;
        return transactions.stream()
                .map(t -> toResponse(t, lookup))
                .toList();
    }

    private TransactionResponse toResponse(Transaction t) {
        String userEmail = familyMemberRepository
                .findByFamilyIdAndUserId(
                        t.getFamily() != null ? t.getFamily().getId() : -1L,
                        t.getUserId())
                .map(m -> m.getUserEmail())
                .orElse(null);
        return buildResponse(t, userEmail);
    }

    private TransactionResponse toResponse(Transaction t, Map<String, String> emailLookup) {
        String userEmail = null;
        if (t.getFamily() != null) {
            userEmail = emailLookup.get(t.getFamily().getId() + ":" + t.getUserId());
        }
        return buildResponse(t, userEmail);
    }

    private TransactionResponse buildResponse(Transaction t, String userEmail) {
        return new TransactionResponse(
                t.getId(),
                t.getAmount(),
                t.getType().name(),
                t.getDescription(),
                t.getDate(),
                t.getUserId(),
                t.getCategory().getName(),
                t.getCategory().getId(),
                t.getFamily() != null ? t.getFamily().getId() : null,
                t.getFamily() != null ? t.getFamily().getName() : null,
                userEmail
        );
    }
}
