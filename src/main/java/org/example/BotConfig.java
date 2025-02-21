package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class BotConfig extends TelegramLongPollingBot {
    final private String BOT_TOKEN = "BOT_TOKEN";
    final private String BOT_NAME = "BOT_NAME";
    private final Map<Long, List<String>> userOptions = new HashMap<>();
    private final Map<Long, String> userStatus = new HashMap<>();
    public BotConfig() {
        super("BOT_TOKEN");
    }
    @Override

    public void onUpdateReceived(Update update) {
        System.out.println("Update received: " + update);
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String userMessage = update.getMessage().getText();
                System.out.println("Received message: " + userMessage);
                Message inMess = update.getMessage();
                Long userId = inMess.getChatId();
                ReplyKeyboardMarkup keyboardMarkup;

                //Получаем текст сообщения пользователя, отправляем в написанный нами обработчик
                String response = parseMessage(userId, inMess.getText());
                //Создаем объект класса SendMessage - наш будущий ответ пользователю
                String status = userStatus.getOrDefault(userId, "none");
                if (status.equals("add") || status.equals("remove")) {
                    keyboardMarkup = initStopKeyboard();
                    System.out.println(status);
                }else keyboardMarkup = initStartKeyboard();
                System.out.println(status);
                if (keyboardMarkup != null) {
                    sendMessageWithKeyboard(response, keyboardMarkup, userId);
                } else {
                    sendMessageWithoutKeyboard(response, userId);
                }
                System.out.println(status);

                //Отправка в чат
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }


    }

    public String parseMessage(Long chatId, String textMsg) throws IOException, URISyntaxException {
        String response = " ";
        switch (textMsg.toLowerCase(Locale.ROOT)) {
            case "/start", "привет", "/help", "помощь.":
                response = """
                        Привет! 🎡 Добро пожаловать в «Колесо Фортуны»!
                        Вот список доступных команд:

                        🎡 Основные команды:
                        🔹 /spin — Крутить колесо и получить случайный вариант.
                        🔹 /list — Посмотреть текущие варианты в колесе.
                        🔹 /reset — Сбросить список вариантов.

                        ➕ Редактирование вариантов:
                        🔹 /addoption текст — Добавить вариант.
                        🔹 /removeoption текст — Удалить вариант.

                        ℹ Дополнительные команды:
                        🔹 /help — Подробное описание работы бота.

                        Крути колесо и получай случайный результат! 🎰
                        """;
                break;
            case "мой профиль.":
                response = "Ваш профиль: \nАйдишка чата: " + chatId;
                break;

            case "/spin", "прокрути", "крути!":
                List<String> options = userOptions.getOrDefault(chatId, new ArrayList<>());
                if (options.isEmpty()) {
                    response = "У вас пока нет вариантов! Добавьте их с помощью /добавить.";
                } else {
                    String randomOption = options.get(new Random().nextInt(options.size()));
                    response = "Выпал вариант: " + randomOption;
                }
                // user.getRandom()
                break;
            case "/list", "список", "список вариантов.":
                options = userOptions.getOrDefault(chatId, new ArrayList<>());
                response =  (options.isEmpty() ? "Список пуст." : "Ваши варианты: " + options);
                //  user.getList();
                break;
            case "/reset", "очистить список":
                userOptions.remove(chatId);
                response = "Ваш список успешно очищен!";
                // тут будет поидеи подтверждение, по типу,
                // хотите ли вы точно удалить, но впринципе, буду использовать метод
                // user.reset();
                break;
            case "/addoption", "добавить.":
                response = "Вы можете добавлять варианты для прокрутки сообщение за сообщением!"+
                        "Когда закончите, нажмите кнопку \"/stop\".";;
                userStatus.put(chatId, "add");
                // тут будет поидеи смена статуса бота на тру фолс
                // если будет тру то будет добавлять варианты до тех пор
                // пока не поменяется командой на фолс.
                break;
            case "/removeoption", "удалить.":
                options = userOptions.getOrDefault(chatId, new ArrayList<>());
                response = "Вы можете удалять варианты для прокрутки сообщение за сообщением!\n" + (options.isEmpty() ? "Список пуст." : "Ваши варианты: " + options) +"Когда закончите, нажмите кнопку \"/stop\".";;;
                userStatus.put(chatId, "remove");
                // тут будет поидеи смена статуса бота на тру фолс
                // если будет тру то будет удалять варианты до тех пор
                // пока не поменяется командой на фолс.
                break;
            case "/stop", "stop":
                response = "Напишите следующую команду!";
                userStatus.put(chatId, "none");
            default:
                String status = userStatus.getOrDefault(chatId, "none");
                if (status.equals("add")) {
                    userOptions.computeIfAbsent(chatId, k -> new ArrayList<>()).add(textMsg);
                    response = "Добавлено: " + textMsg;
                } else if (status.equals("remove")) {
                    List<String> list = userOptions.get(chatId);
                    if (list != null && list.remove(textMsg)) {
                        response = "Удалено: " + textMsg;
                    }
                }
                else
                    response = "Сообщение не распознано, повторите пожалуйста.";
        }
        return response;
    }

    @Override
    public String getBotUsername() {
        return "WheelBot";
    }

    ReplyKeyboardMarkup  initStartKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        ArrayList<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Крути!"));
        row1.add(new KeyboardButton("Список вариантов."));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Мой профиль."));
        row2.add(new KeyboardButton("Помощь."));
        KeyboardRow row3 = new KeyboardRow();
        row2.add(new KeyboardButton("Добавить."));
        row2.add(new KeyboardButton("Удалить."));
        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardRows.add(row3);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;

    }
    ReplyKeyboardMarkup initStopKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("/stop"));

        replyKeyboardMarkup.setKeyboard(Collections.singletonList(row));
        return replyKeyboardMarkup;
    }
    void sendMessageWithKeyboard(String text, ReplyKeyboardMarkup keyboard, Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboard);
        message.enableMarkdown(true);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    void sendMessageWithoutKeyboard(String text, Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.enableMarkdown(true);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}