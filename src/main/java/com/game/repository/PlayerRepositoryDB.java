package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.NativeQuery;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {

    private final SessionFactory sessionFactory;
    public PlayerRepositoryDB() {
        Configuration configuration = new Configuration();
        configuration.configure();
        configuration.addAnnotatedClass(Player.class);
        sessionFactory = configuration.buildSessionFactory();

    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        Session session = sessionFactory.openSession();
        try(session) {
            NativeQuery<Player> qu = session.createNativeQuery("SELECT * FROM rpg.player LIMIT :limit OFFSET :offset", Player.class);
            qu.setParameter("limit", pageSize);
            qu.setParameter("offset", pageNumber * pageSize);
            return qu.list();
        }
    }

    @Override
    public int getAllCount() {
        Session session = sessionFactory.openSession();
        try(session) {
            return Math.toIntExact(session.createNamedQuery("Player.count", Long.class).getSingleResult());
        }

    }

    @Override
    public Player save(Player player) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        try(session) {
            session.persist(player);
            tx.commit();
            return player;
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Player update(Player player) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        try(session) {
            player = session.merge(player);
            tx.commit();
            return player;
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Player> findById(long id) {
        Session session = sessionFactory.openSession();
        try(session) {
            return Optional.ofNullable(session.find(Player.class, id));
        }
    }

    @Override
    public void delete(Player player) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        try(session) {
            session.remove(player);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}