package pro.sky.telegrambot;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
class TelegramBotApplicationTests {
    @Mock
    private NotificationTaskRepository notificationTaskRepository;

    @InjectMocks
    private NotificationTaskService service;

    @Test
    void testParseAndSaveReminderValidInput() throws Exception {
        MockitoAnnotations.openMocks(this);

        String input = "18.04.2025 14:30 Напоминание о встрече";
        Long chatId = 1L;

        service.parseAndSaveReminder(input, chatId);

        NotificationTask expectedTask = new NotificationTask(
                chatId,
                "Напоминание о встрече",
                LocalDateTime.of(2025, 4, 18, 14, 30));

        assertEquals(expectedTask.getChatId(), chatId);
        assertEquals(expectedTask.getTaskText(), "Напоминание о встрече");
        assertEquals(expectedTask.getSendTime(), LocalDateTime.of(2025, 4, 18, 14, 30));
    }

    @Test
    void testParseAndSaveReminderInvalidInput() {
        MockitoAnnotations.openMocks(this);

        String invalidInput = "Некорректный формат строки";
        Long chatId = 1L;

        assertThrows(IllegalArgumentException.class, () -> service.parseAndSaveReminder(invalidInput, chatId));
    }

}
