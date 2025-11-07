package pro.sky.telegrambot.service;

@Service
public class NotificationTaskService {

    private final NotificationTaskRepository repository;
    private final TelegramBotUpdatesListener telegramBotUpdatesListener;

    @Autowired
    public NotificationTaskService(NotificationTaskRepository repository) {
        this.repository = repository;
    }

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


    public void parseAndSaveReminder(String incomingMessage, Long chatId) {
        Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})\\s+(.*)");
        Matcher matcher = pattern.matcher(incomingMessage.trim());

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Неправильный формат сообщения");
        }

        String dateStr = matcher.group(1);
        String reminderText = matcher.group(2);

        LocalDateTime sendTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        NotificationTask task = new NotificationTask(chatId, reminderText, sendTime);

        repository.save(task);
    }

    public NotificationSchedulerService(NotificationTaskRepository repository, TelegramBotUpdatesListener telegramBotUpdatesListener) {
        this.repository = repository;
        this.telegramBotUpdatesListener = telegramBotUpdatesListener;
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void checkNotifications() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> tasks = repository.findBySendTime(now);

        for (NotificationTask task : tasks) {
            botSender.sendMessage(task.getChatId(), task.getTaskText());
            repository.delete(task);
        }
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void sendNotifications() {
        LocalDateTime currentMinute = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> tasks = repository.findBySendTime(currentMinute);

        for (NotificationTask task : tasks) {
            TelegramBotUpdatesListener.sendMessage(task.getChatId(), task.getTaskText());
            repository.delete(task);
        }
    }


}