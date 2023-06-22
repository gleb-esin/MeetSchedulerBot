package com.example.MeetSchedulerBot.service;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Transactional
@Repository
public interface MeetingRepository extends CrudRepository<Meeting, Long> {
    boolean existsByPassphrase(String passphrase);

    boolean existsByChatAndPassphrase(Long chat, String passphrase);

    void deleteByChatAndPassphrase(Long chat, String passphrase);

    @Query(value = "SELECT m.owner FROM Meeting m WHERE m.passphrase = :passphrase AND m.chat = :chat", nativeQuery = true)
    boolean isUserOwner(@Param("chat") Long chat, @Param("passphrase") String passphrase);

    @Query(value = "SELECT m.month FROM Meeting m WHERE m.passphrase = :passphrase AND m.owner = true", nativeQuery = true)
    int findMonthByPassphrase(@Param("passphrase") String passphrase);

    @Query(value = "SELECT m.name FROM Meeting m WHERE m.passphrase = :passphrase AND m.owner = true", nativeQuery = true)
    String findOwnerByPassphrase(@Param("passphrase") String passphrase);

    @Query(value = "SELECT STRING_AGG(m.name, ', ') FROM Meeting m WHERE m.passphrase = :passphrase", nativeQuery = true)
    String concatenateFirstNamesByPassphrase(@Param("passphrase") String passphrase);

    @Query(value = "SELECT STRING_AGG(m.dates, '----') FROM Meeting m WHERE m.passphrase = :passphrase", nativeQuery = true)
    String concatenateDatesByPassphrase(@Param("passphrase") String passphrase);

    @Query(value = "SELECT m.chat FROM Meeting m WHERE m.owner = false AND m.passphrase = :passphrase ORDER BY id LIMIT 1", nativeQuery = true)
    Long whoWillBeNextOwner(@Param("passphrase") String passphrase);

    @Modifying
    @Query("UPDATE Meeting SET owner = true WHERE passphrase = :passphrase AND chat = :chat")
    void setNextOwner(@Param("chat") Long chat, @Param("passphrase") String passphrase);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Meeting m WHERE m.passphrase = :passphrase AND m.owner = false")
    boolean checkPassphraseAndOwner(@Param("passphrase") String passphrase);

    @Query(value = "SELECT STRING_AGG(CAST(m.chat AS VARCHAR), ' ') " +
            "FROM Meeting m " +
            "WHERE m.passphrase = :passphrase " +
            "GROUP BY m.edited " +
            "ORDER BY m.edited DESC", nativeQuery = true)
    List<String> listOfNotified(@Param("passphrase") String passphrase);


}
