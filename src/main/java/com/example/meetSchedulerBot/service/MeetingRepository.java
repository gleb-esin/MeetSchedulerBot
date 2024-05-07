package com.example.meetSchedulerBot.service;

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
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Meeting e WHERE e.chat = :chat AND e.passphrase = :passphrase")
    boolean existsByChatAndPassphrase2(@Param("chat") Long chat, @Param("passphrase") String passphrase);
    boolean existsByChatAndOwner(Long chat, boolean owner);
    boolean existsByChat(Long chat);
    @Query(value = "SELECT m.owner FROM Meeting m WHERE m.passphrase = :passphrase AND m.chat = :chat", nativeQuery = true)
    boolean isUserOwner(@Param("chat") Long chat, @Param("passphrase") String passphrase);

    @Query(value = "SELECT m.month FROM Meeting m WHERE m.passphrase = :passphrase AND m.owner = true", nativeQuery = true)
    int findMonthByPassphrase(@Param("passphrase") String passphrase);
    @Query(value = "SELECT m.name FROM Meeting m WHERE m.passphrase = :passphrase AND m.owner = true", nativeQuery = true)
    String findOwnerByPassphrase(@Param("passphrase") String passphrase);
    @Query(value = "SELECT * FROM meeting WHERE chat = :chatId AND owner = false ORDER BY expired ASC", nativeQuery = true)
    List<Meeting> findMyParticipation(@Param("chatId")Long chat);
    @Query(value = "SELECT * FROM meeting WHERE chat = :chatId AND owner = true ORDER BY expired ASC", nativeQuery = true)
    List<Meeting> findMyMeetings(@Param("chatId") Long chatId);
    @Query(value = "SELECT unnest(m.dates) FROM Meeting m WHERE m.passphrase = :passphrase AND m.chat = :chat", nativeQuery = true)
    List<Integer> findDatesByPassphraseAndChat(@Param("passphrase") String passphrase, @Param("chat") Long chat);
    Meeting findByChatAndPassphrase(Long chat, String passphrase);
    @Query(value = "SELECT m.name FROM Meeting m WHERE m.passphrase = :passphrase AND m.owner = true", nativeQuery = true)
    String findOwnersName(String passphrase);
    @Query(value = "SELECT m.id FROM Meeting m WHERE m.passphrase = :passphrase AND m.owner = true", nativeQuery = true)
    Integer findIdByPassphrase(String passphrase);
    @Query(value = "SELECT m.passphrase FROM Meeting m WHERE m.id = :id", nativeQuery = true)
    String findPassphraseById(Integer id);

    @Query(value = "SELECT STRING_AGG(m.name, ', ') FROM Meeting m WHERE m.passphrase = :passphrase", nativeQuery = true)
    String concatenateFirstNamesByPassphrase(@Param("passphrase") String passphrase);
    @Query(value = "SELECT m.dates FROM Meeting m WHERE m.passphrase = :passphrase", nativeQuery = true)
    List<List<Integer>> concatenateDatesByPassphrase(@Param("passphrase") String passphrase);

    @Query(value = "SELECT m.chat FROM Meeting m WHERE m.owner = false AND m.passphrase = :passphrase ORDER BY id LIMIT 1", nativeQuery = true)
    Long whoWillBeNextOwner(@Param("passphrase") String passphrase);

    @Modifying
    @Query("UPDATE Meeting SET owner = true WHERE passphrase = :passphrase AND chat = :chat")
    void setNextOwner(@Param("chat") Long chat, @Param("passphrase") String passphrase);

    @Query(value = "SELECT STRING_AGG(CAST(m.chat AS VARCHAR), ' ') " +
            "FROM Meeting m " +
            "WHERE m.passphrase = :passphrase ", nativeQuery = true)
    String listOfNotified(@Param("passphrase") String passphrase);

    void deleteByChatAndPassphrase(@Param("chat") Long chat, @Param("passphrase") String passphrase);
    @Query(value = "SELECT remove_expired_passphrases();", nativeQuery = true)
    void deleteExpiredMeetings();
    @Query(value = "SELECT delete_past_dates_in_current_month();", nativeQuery = true)
    void deletePastDate();
    void deleteByPassphrase(String passphrase);
}

