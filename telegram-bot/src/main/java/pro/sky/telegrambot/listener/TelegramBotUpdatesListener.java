package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            Message message = update.message();

            if (message != null && "/start".equalsIgnoreCase(message.text())) {
                long chatId = message.chat().id();

                SendMessage sendMessage = new SendMessage(chatId, "Привет! Я твой новый бот. Готов помогать!");
                telegramBot.execute(sendMessage);

                logger.info("Отправил приветственное сообщение в чат с ID {}", chatId);
            }
        });

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    public TelegramBotUpdatesListener(@Value("${telegram.bot.token}") String token) {
        this.telegramBot = new TelegramBot(token);
    }

    public void sendMessage(long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text);
        telegramBot.execute(request);
    }

}
