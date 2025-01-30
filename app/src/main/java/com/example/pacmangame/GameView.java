package com.example.pacmangame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Random;

public class GameView extends View {
    public static Ghost ghost;
    public static  Ghost ghost2;
    public static  Ghost ghost3;
    public static  Ghost ghost4;
    private long gameStartTime;
    private boolean isFruitSpawningAllowed = false;

    public static int fruitX = -1;
    public static int fruitY = -1;
    private long fruitSpawnTime = 1000;
    private long fruitDuration = 20000; // 5 секунд длительность жизни фрукта
    private Random random = new Random();
    private boolean gameEnded = false;

    private boolean gameFinished = false;

    private Paint paint;
    private int[][] map;
    private int oneBlockSize = 51;
    private Pacman pacman;
    private GestureDetector gestureDetector;

    private int direction = Direction.RIGHT; // Начальное направление
    private Handler handler;
    private Runnable movePacman;

    public static int score = 0; // Переменная для счёта

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gameStartTime = System.currentTimeMillis(); // Сохраняем время старта игры
        paint = new Paint();

        map = new int[][]{
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 3, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 1},
                {1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1},
                {1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1},
                {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                {1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1},
                {1, 2, 2, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2, 2, 2, 1},
                {1, 1, 1, 1, 1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1},
                {0, 0, 0, 0, 1, 2, 1, 0, 0, 0, 0, 0, 0, 0, 1, 2, 1, 0, 0, 0, 0},
                {1, 1, 1, 1, 1, 2, 1, 0, 1, 1, 8, 1, 1, 0, 1, 2, 1, 1, 1, 1, 1},
                {2, 2, 2, 2, 2, 2, 2, 0, 1, 5, 6, 7, 1, 0, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 1, 2, 1, 0, 1, 1, 1, 1, 1, 0, 1, 2, 1, 1, 1, 1, 1},
                {0, 0, 0, 0, 1, 2, 1, 0, 0, 0, 0, 0, 0, 0, 1, 2, 1, 0, 0, 0, 0},
                {1, 1, 1, 1, 1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1},
                {1, 2, 2, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2, 2, 2, 1},
                {1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1},
                {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                {1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1},
                {1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1},
                {1, 3, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };
        initialize();
        gestureDetector = new GestureDetector(context, new GestureListener());

        // Настройка таймера для автоматического движения
        handler = new Handler();
        movePacman = new Runnable() {
            @Override
            public void run() {
                if (pacman != null) {
                    pacman.move(direction);
                    invalidate();
                }
                handler.postDelayed(this, 180); // Обновление каждые 200 мс
            }
        };
        handler.post(movePacman);
    }


    private void initialize() {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                switch (map[y][x]) {
                    case 4:
                        pacman = new Pacman(x * oneBlockSize, y * oneBlockSize, oneBlockSize, oneBlockSize, map, oneBlockSize, this);
                        map[y][x] = 2;
                        break;
                    case 5:
                        ghost = new Ghost(getContext(), x, y, oneBlockSize, map, pacman, 8, "BLUE");
                        map[y][x] = 2;
                        break;
                    case 6:
                        ghost2 = new Ghost(getContext(), x, y, oneBlockSize, map, pacman, 6, "RED");
                        map[y][x] = 2;
                        break;
                    case 7:
                        ghost3 = new Ghost(getContext(), x, y, oneBlockSize, map, pacman, 5, "YELLOW");
                        map[y][x] = 2;
                        break;
                    case 8:
                        ghost4 = new Ghost(getContext(), x, y, oneBlockSize, map, pacman, 7, "GREEN");                        map[y][x] = 2;
                        break;
                }
            }
        }
    }

    private boolean areAllDotsCollected() {
        for (int[] row : map) {
            for (int cell : row) {
                if (cell == 2) {
                    return false;
                }
            }
        }
        return true;
    }
    private void spawnFruit() {
        int col, row;
        do {
            col = random.nextInt(map[0].length);
            row = random.nextInt(map.length);
        } while (map[row][col] != 2); // Спавнится только на пустых местах

        fruitX = col;
        fruitY = row;
        fruitSpawnTime = System.currentTimeMillis();
    }

    private void goToScoreScreen() {
        Context context = getContext();
        Intent intent = new Intent(context, ScoreActivity.class);
        intent.putExtra("SCORE", score); // Передаем текущий счет на экран рекордов
        context.startActivity(intent);
        ((Activity) context).finish(); // Закрыть текущую активность
    }


    private void endGame() {
        if (gameEnded) return; // Если игра уже завершена, не выполняем повторно
        gameEnded = true; // Помечаем игру завершенной
        direction = Direction.NONE;

        if (pacman != null) {
            pacman.setGameFinished(true);
        }

        Toast.makeText(getContext(), "Игра завершена!", Toast.LENGTH_LONG).show();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                goToScoreScreen();
            }
        }, 2000);
    }


    public void increaseScore(int points) {
        score += points;

        if (areAllDotsCollected()) {
            endGame();
        }

        invalidate(); // Перерисовать View, чтобы обновить отображение счёта
    }
    void makeGhostsVulnerable() {
        ghost.setVulnerable(true);
        ghost2.setVulnerable(true);
        ghost3.setVulnerable(true);
        ghost4.setVulnerable(true);
        invalidate(); // Запросить обновление экрана
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);

        // Отрисовка карты
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] == 1) { // стена
                    paint.setColor(Color.BLUE);
                    canvas.drawRect(
                            j * oneBlockSize,
                            i * oneBlockSize,
                            (j + 1) * oneBlockSize,
                            (i + 1) * oneBlockSize,
                            paint
                    );
                } else if (map[i][j] == 2) { // точка
                    paint.setColor(Color.WHITE);
                    canvas.drawCircle(
                            j * oneBlockSize + oneBlockSize / 2,
                            i * oneBlockSize + oneBlockSize / 2,
                            oneBlockSize / 3,
                            paint
                    );
                }
                if (map[i][j] == 3) { // силовая пеллета
                    paint.setColor(Color.WHITE);
                    canvas.drawCircle(
                            j * oneBlockSize + oneBlockSize / 2,
                            i * oneBlockSize + oneBlockSize / 2,
                            oneBlockSize / 2, // больше, чем обычные точки
                            paint
                    );
                }
            }
        }

        // Отрисовка Pacman
        if (pacman != null) {
            pacman.draw(canvas);
        }

        paint.setColor(Color.YELLOW);
        paint.setTextSize(50);
        canvas.drawText("Score: " + score, 20, 50, paint);

        if (!gameFinished && areAllDotsCollected()) {
            gameFinished = true;
            paint.setTextSize(100);
            canvas.drawText("YOU WIN!", getWidth() / 2 - 150, getHeight() / 2, paint);
            endGame();
        }
        // Проверка, прошло ли 60 секунд с момента начала игры
        if (!isFruitSpawningAllowed && System.currentTimeMillis() - gameStartTime > 60000) {
            isFruitSpawningAllowed = true; // Разрешаем спавн фруктов после минуты
        }
        // Движение и отрисовка призрака
// Движение и отрисовка всех призраков
        if (ghost != null) {
            ghost.move();
            ghost.draw(canvas, paint);
            if (ghost.checkCollision(Pacman.x, Pacman.y)) {
                gameFinished = true;
                endGame(); // Закончить игру, если Pacman столкнулся с призраком
            }
        }

        if (ghost2 != null) {
            ghost2.move();
            ghost2.draw(canvas, paint);
            if (ghost2.checkCollision(Pacman.x, Pacman.y)) {
                gameFinished = true;
                endGame(); // Закончить игру, если Pacman столкнулся с призраком
            }
        }

        if (ghost3 != null) {
            ghost3.move();
            ghost3.draw(canvas, paint);
            if (ghost3.checkCollision(Pacman.x, Pacman.y)) {
                gameFinished = true;
                endGame(); // Закончить игру, если Pacman столкнулся с призраком
            }
        }

        if (ghost4 != null) {
            ghost4.move();
            ghost4.draw(canvas, paint);
            if (ghost4.checkCollision(Pacman.x, Pacman.y)) {
                gameFinished = true;
                endGame(); // Закончить игру, если Pacman столкнулся с призраком
            }
        }


        // Отрисовка фрукта
        if (fruitX != -1 && fruitY != -1) {
            paint.setColor(Color.RED);
            canvas.drawCircle(
                    fruitX * oneBlockSize + oneBlockSize / 2,
                    fruitY * oneBlockSize + oneBlockSize / 2,
                    oneBlockSize / 2,
                    paint
            );

            // Если фрукт на поле уже больше 5 секунд, удаляем его
            if (System.currentTimeMillis() - fruitSpawnTime > fruitDuration) {
                fruitX = -1;
                fruitY = -1;
            }
        }
        // Спавн фрукта каждые 10 секунд
        if (isFruitSpawningAllowed && fruitX == -1 && fruitY == -1 && System.currentTimeMillis() - fruitSpawnTime > 10000) {
            spawnFruit();
        }
        if (ghost.checkCollision(Pacman.x, Pacman.y)) {
            gameFinished = true;
            endGame(); // Закончить игру, если Pacman столкнулся с призраком
        }
        if (!gameEnded && ghost.checkCollision(Pacman.x, Pacman.y)) {
            endGame();
        }




    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                // Горизонтальные свайпы
                if (distanceX > 0) {
                    direction = Direction.LEFT;
                } else {
                    direction = Direction.RIGHT;
                }
            } else {
                // Вертикальные свайпы
                if (distanceY > 0) {
                    direction = Direction.UP;
                } else {
                    direction = Direction.DOWN;
                }
            }
            return true;
        }
    }
}