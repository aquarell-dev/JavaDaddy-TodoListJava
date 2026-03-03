package com.aquarell.service.tasks;

import com.aquarell.dto.tasks.TaskCreate;
import com.aquarell.dto.tasks.TaskUpdate;
import com.aquarell.entity.tasks.Task;
import com.aquarell.entity.tasks.TaskStatus;
import com.aquarell.exceptions.tasks.InvalidDeadlineException;
import com.aquarell.exceptions.tasks.TaskNotFoundException;
import com.aquarell.repository.tasks.ITasksRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TasksServiceTest {

    @Mock
    private ITasksRepository tasksRepository;

    @InjectMocks
    private TasksService tasksService;

    @Captor
    private ArgumentCaptor<Task> taskCaptor;

    @Captor
    private ArgumentCaptor<Comparator<Task>> comparatorCaptor;

    @Captor
    private ArgumentCaptor<Predicate<Task>> predicateCaptor;

    @Test
    void create_shouldThrowInvalidDeadlineException_whenDeadlineIsInPast() {
        // Arrange
        TaskCreate taskCreate = new TaskCreate(
                "Task",
                "Description",
                LocalDate.now().minusDays(1)
        );

        // Act + Assert
        assertThatThrownBy(() -> tasksService.create(taskCreate))
                .isInstanceOf(InvalidDeadlineException.class)
                .hasMessageContaining("Дедлайн не может быть в прошлом");

        verifyNoInteractions(tasksRepository);
    }

    @Test
    void create_shouldSaveTaskWithGeneratedIdAndTodoStatus_whenDeadlineIsValid() {
        // Arrange
        LocalDate deadline = LocalDate.now().plusDays(7);
        TaskCreate taskCreate = new TaskCreate("Task", "Description", deadline);

        when(tasksRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Task.class));

        // Act
        Task created = tasksService.create(taskCreate);

        // Assert
        verify(tasksRepository).save(taskCaptor.capture());
        Task saved = taskCaptor.getValue();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(taskCreate.name());
        assertThat(saved.getDescription()).isEqualTo(taskCreate.description());
        assertThat(saved.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(saved.getDeadline()).isEqualTo(taskCreate.deadline());

        assertThat(created.getId()).isEqualTo(saved.getId());
        assertThat(created.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void deleteById_shouldDelegateToRepository() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        tasksService.deleteById(id);

        // Assert
        verify(tasksRepository).deleteById(id);
        verifyNoMoreInteractions(tasksRepository);
    }

    @Test
    void findAll_shouldReturnRepositoryResult() {
        // Arrange
        List<Task> tasks = List.of(mock(Task.class), mock(Task.class));
        when(tasksRepository.findAll()).thenReturn(tasks);

        // Act
        List<Task> result = tasksService.findAll();

        // Assert
        assertThat(result).isSameAs(tasks);
        verify(tasksRepository).findAll();
        verifyNoMoreInteractions(tasksRepository);
    }

    @Test
    void sortByStatus_shouldCallRepositoryFindAllWithStatusComparator() {
        // Arrange
        List<Task> expected = List.of(mock(Task.class));
        when(tasksRepository.findAll(any(Comparator.class))).thenReturn(expected);

        // Act
        List<Task> result = tasksService.sortByStatus();

        // Assert
        assertThat(result).isSameAs(expected);

        verify(tasksRepository).findAll(comparatorCaptor.capture());
        Comparator<Task> comparator = comparatorCaptor.getValue();
        assertThat(comparator).isNotNull();

        // deterministic check
        Task todo = new Task(UUID.randomUUID(), "a", null, TaskStatus.TODO, LocalDate.now().plusDays(1));
        Task done = new Task(UUID.randomUUID(), "b", null, TaskStatus.DONE, LocalDate.now().plusDays(1));

        assertThat(comparator.compare(todo, done))
                .isEqualTo(todo.getStatus().compareTo(done.getStatus()));
    }

    @Test
    void sortByDeadline_shouldCallRepositoryFindAllWithDeadlineComparator() {
        // Arrange
        List<Task> expected = List.of(mock(Task.class));
        when(tasksRepository.findAll(any(Comparator.class))).thenReturn(expected);

        // Act
        List<Task> result = tasksService.sortByDeadline();

        // Assert
        assertThat(result).isSameAs(expected);

        verify(tasksRepository).findAll(comparatorCaptor.capture());
        Comparator<Task> comparator = comparatorCaptor.getValue();
        assertThat(comparator).isNotNull();

        Task early = new Task(UUID.randomUUID(), "e", null, TaskStatus.TODO, LocalDate.now().plusDays(1));
        Task late = new Task(UUID.randomUUID(), "l", null, TaskStatus.TODO, LocalDate.now().plusDays(10));

        assertThat(comparator.compare(early, late)).isLessThan(0);
        assertThat(comparator.compare(late, early)).isGreaterThan(0);
        assertThat(comparator.compare(early, early)).isZero();
    }

    @Test
    void filterByStatus_shouldCallRepositoryFindAllWithPredicateThatMatchesStatus() {
        // Arrange
        TaskStatus status = TaskStatus.IN_PROGRESS;
        List<Task> expected = List.of(mock(Task.class));
        when(tasksRepository.findAll(any(Predicate.class))).thenReturn(expected);

        // Act
        List<Task> result = tasksService.filterByStatus(status);

        // Assert
        assertThat(result).isSameAs(expected);

        verify(tasksRepository).findAll(predicateCaptor.capture());
        Predicate<Task> predicate = predicateCaptor.getValue();
        assertThat(predicate).isNotNull();

        Task matching = new Task(UUID.randomUUID(), "m", null, TaskStatus.IN_PROGRESS, LocalDate.now().plusDays(1));
        Task nonMatching = new Task(UUID.randomUUID(), "n", null, TaskStatus.DONE, LocalDate.now().plusDays(1));

        assertThat(predicate.test(matching)).isTrue();
        assertThat(predicate.test(nonMatching)).isFalse();
    }

    @Test
    void update_shouldSaveTaskWithSameIdAndUpdatedFields_whenTaskExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        Task existing = new Task(id, "old", "old", TaskStatus.TODO, LocalDate.now().plusDays(1));

        TaskUpdate update = new TaskUpdate(
                "new",
                "new-desc",
                TaskStatus.DONE,
                LocalDate.now().plusDays(30)
        );

        when(tasksRepository.findById(id)).thenReturn(existing);
        when(tasksRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Task.class));

        // Act
        Task updated = tasksService.update(id, update);

        // Assert
        verify(tasksRepository).findById(id);
        verify(tasksRepository).save(taskCaptor.capture());

        Task saved = taskCaptor.getValue();
        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getName()).isEqualTo(update.name());
        assertThat(saved.getDescription()).isEqualTo(update.description());
        assertThat(saved.getStatus()).isEqualTo(update.status());
        assertThat(saved.getDeadline()).isEqualTo(update.deadline());

        assertThat(updated.getId()).isEqualTo(id);
        assertThat(updated.getStatus()).isEqualTo(update.status());
    }

    @Test
    void update_shouldThrowNoSuchElementException_whenTaskDoesNotExist() {
        // Arrange
        UUID id = UUID.randomUUID();
        TaskUpdate update = new TaskUpdate("x", "y", TaskStatus.TODO, LocalDate.now().plusDays(1));

        when(tasksRepository.findById(id)).thenThrow(NoSuchElementException.class);

        // Act + Assert
        assertThatThrownBy(() -> tasksService.update(id, update))
                .isInstanceOf(TaskNotFoundException.class);

        verify(tasksRepository).findById(id);
        verifyNoMoreInteractions(tasksRepository);
    }
}