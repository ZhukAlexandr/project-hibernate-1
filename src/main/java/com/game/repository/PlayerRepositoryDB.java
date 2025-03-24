package com.game.repository;

import com.game.entity.Player;
import jakarta.persistence.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {

    public static final String SELECT_FROM_PLAYER_LIMIT_OFFSET = "SELECT * FROM rpg.player   LIMIT :limit OFFSET  :offset";
    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        this.sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {

        Transaction tx = null;
        try(Session session = sessionFactory.openSession()){
            tx = session.beginTransaction();
            Query query = session.createNativeQuery(SELECT_FROM_PLAYER_LIMIT_OFFSET, Player.class);
            query.setParameter("offset", pageNumber * pageSize);
            query.setParameter("limit", pageSize);
            List<Player> players = query.getResultList();
            tx.commit();
            return players;
        } catch (Exception e) {
            Objects.requireNonNull(tx).rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getAllCount() {
        Transaction tx = null;

        try(Session session = sessionFactory.openSession()){

            tx = session.beginTransaction();
            Query query = session.createNamedQuery("Player_Count", Long.class);
            int playerCount = ((Long)query.getSingleResult()).intValue();
            tx.commit();
            return playerCount;
        } catch (Exception e) {
            Objects.requireNonNull(tx).rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Player save(Player player) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.persist(player);
            tx.commit();
            return player;
        } catch (Exception e) {
            Objects.requireNonNull(tx).rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Player update(Player player) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            Player updatedPlayer = (Player) session.merge(player);
            transaction.commit();
            return updatedPlayer;
        } catch (Exception e) {
            Objects.requireNonNull(transaction).rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Player> findById(long id) {
        Transaction tx = null;
        try(Session session = sessionFactory.openSession()){
            tx = session.beginTransaction();
            return Optional.ofNullable(session.get(Player.class, id));
        } catch (Exception e){
            Objects.requireNonNull(tx).rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Player player) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.delete(player);
            tx.commit();
        } catch (Exception e) {
            Objects.requireNonNull(tx).rollback();
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}