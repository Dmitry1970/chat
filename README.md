1. Запускаем сервер - создаётся ServerSocket. Он запускается и начинает слушать порт 8189. Сидим и ждём когда кто-нибудь откроет соединение по порту 8189.   
2. Клиент(Main) запускает своё окошко. Он открывает сетевое соединение(Socket). Когда создаём Socket мы должны указать куда(с кем) мы хотим подключиться - IP localhost, порт 8189. Открывается соединение(socket). Как  открыли соединение, появляется два потока - входящий(InputStream), исходящий(OutputStream).  
3. На сервере-открываем соединение, говорим что клиент подключился и ждём сообщение.
4. На клиенте отправляем сообщение. Сервер получает сообщение, вычитывает его и печатает в консоль. Данные из потока исчезают. И так далее в бесконечном цикле

