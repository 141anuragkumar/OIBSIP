package com.library.repository;

import com.library.model.Book;
import com.library.model.IssueRecord;
import com.library.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IssueRecordRepository extends JpaRepository<IssueRecord, Long> {
    List<IssueRecord> findByStatus(IssueRecord.Status status);
    List<IssueRecord> findByMemberOrderByIssueDateDesc(Member member);
    List<IssueRecord> findByMemberAndStatus(Member member, IssueRecord.Status status);
    Optional<IssueRecord> findFirstByBookAndMemberAndStatus(Book book, Member member, IssueRecord.Status status);
    long countByBookAndStatus(Book book, IssueRecord.Status status);
    List<IssueRecord> findByFinePaidFalseAndFineAmountGreaterThan(double amount);
}
