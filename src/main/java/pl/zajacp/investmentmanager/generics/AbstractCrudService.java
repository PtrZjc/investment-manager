package pl.zajacp.investmentmanager.generics;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public abstract class AbstractCrudService<T> {

    protected JpaRepository<T,Long> repo;

    public AbstractCrudService() {
    }

    public AbstractCrudService(JpaRepository<T,Long> repo) {
        this.repo = repo;
    }


    public Optional<T> findById(long id) {
        return repo.findById(id);
    }

    public List<T> findAll() {
        return repo.findAll();
    }

    public void save(T entity) {
        repo.save(entity);
    }

    public void delete(T entity) {
        repo.delete(entity);
    }

    public void deleteAll() {
        repo.deleteAll();
    }
}