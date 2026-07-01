package com.example.studentsearch.controller;

import com.example.studentsearch.model.Student;
import com.example.studentsearch.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Year;
import java.util.List;
import java.util.stream.IntStream;

@Controller
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/")
    public String showSearchPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer enrollmentYear,
            Model model) {

        try {
            List<Student> students = studentService.searchStudents(keyword, status, enrollmentYear);
            model.addAttribute("students", students);
            model.addAttribute("hasSearched", true);
        } catch (Exception exception) {
            model.addAttribute("students", List.of());
            model.addAttribute("errorMessage", "Unable to load student records. Please try again.");
        }

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedEnrollmentYear", enrollmentYear);
        addSharedModelAttributes(model);

        if (!model.containsAttribute("newStudent")) {
            model.addAttribute("newStudent", new Student());
        }

        return "index";
    }

    @PostMapping("/students")
    public String addStudent(
            @Valid @ModelAttribute("newStudent") Student newStudent,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("students", studentService.searchStudents(null, null, null));
            model.addAttribute("errorMessage", "Please fix the highlighted fields before saving the student.");
            addSharedModelAttributes(model);
            return "index";
        }

        try {
            studentService.saveStudent(newStudent);
            redirectAttributes.addFlashAttribute("successMessage", "Student record added successfully.");
            return "redirect:/";
        } catch (Exception exception) {
            model.addAttribute("students", studentService.searchStudents(null, null, null));
            model.addAttribute("errorMessage", "Unable to save student. Check that the email address is not already used.");
            addSharedModelAttributes(model);
            return "index";
        }
    }

    private void addSharedModelAttributes(Model model) {
        model.addAttribute("statuses", List.of("Active", "Graduated", "Suspended"));
        model.addAttribute("enrollmentYears", getEnrollmentYearOptions());
    }

    private List<Integer> getEnrollmentYearOptions() {
        int currentYear = Year.now().getValue();
        return IntStream.rangeClosed(currentYear - 8, currentYear)
                .boxed()
                .sorted((first, second) -> Integer.compare(second, first))
                .toList();
    }
}
