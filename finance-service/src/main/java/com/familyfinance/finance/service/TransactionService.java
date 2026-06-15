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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

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

        if (family != null) {
            notificationService.notifyFamilyUpdate(family.getId());
        }

        return toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getPersonalTransactions(Long userId, LocalDate from, LocalDate to,
                                                              OperationType type, Long categoryId) {
        if (type == null && categoryId == null) {
            return transactionRepository.findPersonalByDate(userId, orMin(from), orMax(to))
                    .stream().map(this::toResponse).toList();
        }
        return transactionRepository.findPersonal(userId, from, to, type, categoryId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getFamilyTransactions(Long familyId, Long userId,
                                                            LocalDate from, LocalDate to,
                                                            OperationType type, Long categoryId,
                                                            Long filterUserId) {
        accessService.checkFamilyMember(familyId, userId);
        if (type == null && categoryId == null) {
            return transactionRepository.findByFamilyByDate(familyId, orMin(from), orMax(to), filterUserId)
                    .stream().map(this::toResponse).toList();
        }
        return transactionRepository.findByFamily(familyId, from, to, type, categoryId, filterUserId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllAccessibleTransactions(Long userId, List<Long> familyIds,
                                                                   LocalDate from, LocalDate to) {
        return transactionRepository.findAllAccessibleSafe(userId, familyIds, orMin(from), orMax(to))
                .stream().map(this::toResponse).toList();
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

    private TransactionResponse toResponse(Transaction t) {
        String userEmail = familyMemberRepository
                .findByFamilyIdAndUserId(
                        t.getFamily() != null ? t.getFamily().getId() : -1L,
                        t.getUserId())
                .map(m -> m.getUserEmail())
                .orElse(null);

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
