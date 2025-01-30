package com.example.pacmangame;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Pacman {

    private boolean gameFinished = false; // Флаг завершения игры
    public static float x, y;
    private float width, height;
    private Paint paint;
    private int[][] map;
    private int blockSize;
    private GameView gameView; // Ссылка на GameView

    private Bitmap pacmanOpen, pacmanClosed; // Для спрайтов Pacman
    private long lastFrameChangeTime = 0; // Время последней смены кадра
    private boolean isOpen = true; // Флаг для определения текущего кадра (открыт или закрыт рот)

    private int currentDirection = Direction.RIGHT; // Текущее направление Pacman

    public Pacman(float x, float y, float width, float height, int[][] map, int blockSize, GameView gameView) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.map = map;
        this.blockSize = blockSize;
        this.gameView = gameView;
        paint = new Paint();
        paint.setColor(Color.YELLOW); // Цвет для Pacman

        // Инициализация спрайтов
        try {
            // Загружаем спрайты
            Bitmap originalPacmanOpen = BitmapFactory.decodeResource(gameView.getResources(), R.drawable.pacman_sprite);
            Bitmap originalPacmanClosed = BitmapFactory.decodeResource(gameView.getResources(), R.drawable.pacman_sprite2);

            // Масштабируем спрайты до размеров Pacman
            pacmanOpen = Bitmap.createScaledBitmap(originalPacmanOpen, (int) width, (int) height, true);
            pacmanClosed = Bitmap.createScaledBitmap(originalPacmanClosed, (int) width, (int) height, true);

            // Освобождаем память от оригинальных изображений, если они больше не нужны
            originalPacmanOpen.recycle();
            originalPacmanClosed.recycle();
        } catch (Exception e) {
            Log.e("Pacman", "Error loading or scaling sprite", e);
        }
    }

    public void setGameFinished(boolean isFinished) {
        this.gameFinished = isFinished;
    }

    public void draw(Canvas canvas) {
        if (pacmanOpen == null || pacmanClosed == null) {
            Log.e("Pacman", "Sprites are not loaded");
            return;
        }

        // Меняем кадры спрайта каждую 1/2 секунды
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameChangeTime >= 300) { // Сменить кадр через 300 миллисекунд
            isOpen = !isOpen; // Переключаем состояние между открытым и закрытым ртом
            lastFrameChangeTime = currentTime;
        }

        // Рисуем соответствующий кадр
        Bitmap currentPacman = isOpen ? pacmanOpen : pacmanClosed;

        // Применяем трансформацию в зависимости от направления
        Matrix matrix = new Matrix();
        switch (currentDirection) {
            case Direction.LEFT:
                // Отражение по горизонтали
                matrix.postScale(-1, 1, currentPacman.getWidth() / 2f, currentPacman.getHeight() / 2f);
                break;
            case Direction.UP:
                // Поворот на 270 градусов
                matrix.postRotate(270, currentPacman.getWidth() / 2f, currentPacman.getHeight() / 2f);
                break;
            case Direction.DOWN:
                // Поворот на 90 градусов
                matrix.postRotate(90, currentPacman.getWidth() / 2f, currentPacman.getHeight() / 2f);
                break;
            // Для Direction.RIGHT трансформация не требуется
        }

        // Создаем трансформированный спрайт
        Bitmap transformedPacman = Bitmap.createBitmap(currentPacman, 0, 0, currentPacman.getWidth(), currentPacman.getHeight(), matrix, true);

        // Рисуем спрайт
        canvas.drawBitmap(transformedPacman, x, y, paint);
    }

    public void move(int direction) {
        if (gameFinished) return; // Остановить движение, если игра завершена

        // Обновляем текущее направление
        currentDirection = direction;

        float newX = x;
        float newY = y;

        switch (direction) {
            case Direction.LEFT:
                newX -= blockSize;
                break;
            case Direction.RIGHT:
                newX += blockSize;
                break;
            case Direction.UP:
                newY -= blockSize;
                break;
            case Direction.DOWN:
                newY += blockSize;
                break;
        }

        // Проверка на телепортацию
        if (newX < 0) {
            newX = map[0].length * blockSize - width;
        } else if (newX >= map[0].length * blockSize) {
            newX = 0;
        }

        if (newY < 0) {
            newY = map.length * blockSize - height;
        } else if (newY >= map.length * blockSize) {
            newY = 0;
        }

        if (canMoveTo(newX, newY)) {
            x = newX;
            y = newY;
        }
        collectDots();
    }

    private void collectDots() {
        int col = (int) (x / blockSize);
        int row = (int) (y / blockSize);

        if (row >= 0 && row < map.length && col >= 0 && col < map[0].length) {
            if (map[row][col] == 2) { // Если Pacman на точке
                map[row][col] = 0; // Убираем точку с карты
                GameView.score += 10;// Увеличиваем счёт
            }
        }
        // Проверка сбора фрукта
        if (row == gameView.fruitY && col == gameView.fruitX) {
            gameView.fruitX = -1;
            gameView.fruitY = -1;
            GameView.score += 25; // Добавляем 25 очков за фрукт
        }

        if (map[row][col] == 3) { // Pacman подобрал силовую пеллету
            try {
                map[row][col] = 2;// удаляем пеллету с карты
                GameView.ghost.setVulnerable(true);  // делаем первого призрака уязвимым
                GameView.ghost2.setVulnerable(true); // делаем второго призрака уязвимым
                GameView.ghost3.setVulnerable(true); // делаем третьего призрака уязвимым
                GameView.ghost4.setVulnerable(true); // делаем четвертого призрака уязвимым
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean canMoveTo(float newX, float newY) {
        int newCol = (int) (newX / blockSize);
        int newRow = (int) (newY / blockSize);

        if (newRow < 0 || newRow >= map.length || newCol < 0 || newCol >= map[0].length) {
            return false;
        }

        return map[newRow][newCol] != 1; // 1 обозначает стены
    }
}