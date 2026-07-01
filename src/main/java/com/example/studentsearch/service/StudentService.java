package com.example.studentsearch.service;

import com.example.studentsearch.model.Student;
import com.example.studentsearch.repository.StudentRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> searchStudents(String keyword, String status, Integer enrollmentYear) {
        Specification<Student> specification = buildSearchSpecification(keyword, status, enrollmentYear);
        Sort sort = Sort.by(Sort.Direction.ASC, "lastName", "firstName");
        return studentRepository.findAll(specification, sort);
    }

    public Student saveStudent(Student student) {
        student.setFirstName(student.getFirstName().trim());
        student.setLastName(student.getLastName().trim());
        student.setEmail(student.getEmail().trim().toLowerCase());
        student.setDepartment(student.getDepartment().trim());
        student.setStatus(student.getStatus().trim());
        return studentRepository.save(student);
    }

    private Specification<Student> buildSearchSpecification(String keyword, String status, Integer enrollmentYear) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String normalizedKeyword = keyword.trim().toLowerCase();
                String likePattern = "%" + normalizedKeyword + "%";
                List<Predicate> keywordPredicates = new ArrayList<>();

                keywordPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likePattern));
                keywordPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likePattern));
                keywordPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("department")), likePattern));

                // If the keyword is numeric, allow exact Student ID searches from the same search box.
                try {
                    Long id = Long.parseLong(normalizedKeyword);
                    keywordPredicates.add(criteriaBuilder.equal(root.get("id"), id));
                } catch (NumberFormatException ignored) {
                    // Non-numeric keywords simply search name and department fields.
                }

                predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new Predicate[0])));
            }

            if (StringUtils.hasText(status)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("status")), status.trim().toLowerCase()));
            }

            if (enrollmentYear != null) {
                predicates.add(criteriaBuilder.equal(root.get("enrollmentYear"), enrollmentYear));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
