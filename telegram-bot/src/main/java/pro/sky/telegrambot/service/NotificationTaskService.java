package pro.sky.telegrambot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NotificationTaskService {

    private final NotificationTaskRepository repository;
    private final TelegramBotUpdatesListener telegramBotUpdatesListener;

    public NotificationTask saveTask(NotificationTask task) {
        return repository.save(task);
    }

    public Optional<NotificationTask> getTaskById(Long id) {
        return repository.findById(id);
    }

    public List<NotificationTask> getAllTasks() {
        return repository.findAll();
    }

    public void deleteTask(Long id) {
        repository.deleteById(id);
    }

    public void parseAndSaveReminder(String incomingMessage, Long chatId) throws Exception {
        Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})\\s+(.*)");
        Matcher matcher = pattern.matcher(incomingMessage.trim());

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Неверный формат сообщения.");
        }

        String dateStr = matcher.group(1);
        String reminderText = matcher.group(2);

        LocalDateTime sendTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        NotificationTask task = new NotificationTask(chatId, reminderText, sendTime);
        repository.save(task);
    }

    @Transactional
    @Scheduled(cron = "0 */1 * * * *")
    public void sendNotifications() {
        LocalDateTime currentMinute = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
        List<NotificationTask> tasks = repository.findBySendTime(currentMinute);

        for (NotificationTask task : tasks) {
            telegramBotUpdatesListener.sendMessage(task.getChatId(), task.getTaskText());
            repository.delete(task);
        }
    }

}