package br.com.rocketseatjava.todolist.task;

import br.com.rocketseatjava.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping
    public ResponseEntity<Object> list(HttpServletRequest request){
        var idUser = request.getAttribute("idUser");
        List<TaskModel> tasks = taskRepository.findByIdUser((UUID) idUser);
        return ResponseEntity.ok().body(tasks);
    }

    @PostMapping("/")
    public ResponseEntity<Object> create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        taskModel.setIdUser((UUID) request.getAttribute("idUser"));

        LocalDateTime currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Data de início/fim devem ser maiores ou igual a data atual!");
        }
        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Data de início deve ser menor que data de término!");
        }

        TaskModel savedTask = taskRepository.save(taskModel);
        return ResponseEntity.ok().body(savedTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@RequestBody TaskModel taskModel,
                                         HttpServletRequest request, @PathVariable UUID id) {
        Optional<TaskModel> task = taskRepository.findById(id);

        if (task.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tarefa não encontrada!");
        if (!task.get().getIdUser().equals(request.getAttribute("idUser")))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário não possui permissão para alterar a tarefa!");


        Utils.copyNonNullProperties(taskModel, task.get());
        TaskModel saved = taskRepository.save(task.get());
        return ResponseEntity.ok().body(saved);
    }

}
