package com.example.pacmangame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Comparator;

public class Ghost {

    private long vulnerabilityDuration = 5000; // Время уязвимости в миллисекундах (5 секунд)
    private long vulnerabilityStartTime = 0; // Время начала уязвимости

    private boolean isVulnerable = false;
    private float x, y;
    private int blockSize;
    private int[][] map;
    private Pacman pacman;
    private Random random;
    private int range; // Радиус преследования
    private String colorName; // Имя цвета призрака
    private int startX, startY; // Начальные координаты

    private Bitmap ghostSprite; // Спрайт призрака
    private Bitmap vulnerableSprite; // Спрайт уязвимого призрака

    private Context context;

    // Конструктор
    public Ghost(Context context, int startX, int startY, int blockSize, int[][] map, Pacman pacman, int range, String colorName) {
        this.context = context;
        this.startX = startX;
        this.startY = startY;
        this.x = startX * blockSize;
        this.y = startY * blockSize;
        this.blockSize = blockSize;
        this.pacman = pacman;
        this.map = map;
        this.random = new Random();
        this.range = range;
        this.colorName = colorName;

        // Загрузка спрайтов
        this.ghostSprite = loadGhostSprite(colorName);
        this.vulnerableSprite = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(context.getResources(), R.drawable.cyan),
                blockSize, blockSize, true
        );

        if (map == null || map.length == 0 || map[0].length == 0) {
            throw new IllegalArgumentException("Map is not properly initialized.");
        }
    }

    // Метод загрузки спрайтов призраков по цвету
    private Bitmap loadGhostSprite(String colorName) {
        int resId;
        switch (colorName.toUpperCase()) {
            case "RED":
                resId = R.drawable.red;
                break;
            case "BLUE":
                resId = R.drawable.blue;
                break;
            case "GREEN":
                resId = R.drawable.green;
                break;
            case "YELLOW":
                resId = R.drawable.yellow;
                break;
            default:
                resId = R.drawable.cyan;
        }
        Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        return Bitmap.createScaledBitmap(originalBitmap, blockSize, blockSize, true);
    }


    // Геттер для isVulnerable
    public boolean isVulnerable() {
        return isVulnerable;
    }

    // Сеттер для isVulnerable
    public void setVulnerable(boolean vulnerable) {
        isVulnerable = vulnerable;
        if (vulnerable) {
            vulnerabilityStartTime = System.currentTimeMillis(); // Запоминаем время начала уязвимости
        }
    }

    // Движение призрака
    public void move() {
        if (isVulnerable) {
            // Проверяем, не истекло ли время уязвимости
            if (System.currentTimeMillis() - vulnerabilityStartTime > vulnerabilityDuration) {
                setVulnerable(false); // Возвращаем призрака в нормальное состояние
            } else {
                moveToRandomPosition(); // Двигаемся случайным образом, если уязвим
            }
        } else if (isInRange()) {
            // Преследуем Pacman
            int startX = (int) (x / blockSize);
            int startY = (int) (y / blockSize);
            int goalX = (int) (Pacman.x / blockSize);
            int goalY = (int) (Pacman.y / blockSize);

            Node nextStep = findPath(startX, startY, goalX, goalY);
            if (nextStep != null) {
                this.x = nextStep.x * blockSize;
                this.y = nextStep.y * blockSize;
            }
        } else {
            // Случайное движение, если Pacman вне диапазона
            moveToRandomPosition();
        }
    }



    // Двигаемся к случайной позиции
    private void moveToRandomPosition() {
        Node randomTarget = getRandomWalkableNode();
        if (randomTarget != null) {
            int startX = (int) (x / blockSize);
            int startY = (int) (y / blockSize);
            Node nextStep = findPath(startX, startY, randomTarget.x, randomTarget.y);
            if (nextStep != null) {
                this.x = nextStep.x * blockSize;
                this.y = nextStep.y * blockSize;
            }
        }
    }

    // Находим случайную допустимую позицию на карте
    private Node getRandomWalkableNode() {
        int rows = map.length;
        int cols = map[0].length;

        List<Node> walkableNodes = new ArrayList<>();

        // Собираем все доступные (не являющиеся стенами) позиции
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (map[row][col] != 1) { // Проверка, что это не стена
                    walkableNodes.add(new Node(col, row, null, 0, 0));
                }
            }
        }

        // Если есть доступные позиции, выбираем случайную
        if (!walkableNodes.isEmpty()) {
            return walkableNodes.get(random.nextInt(walkableNodes.size()));
        }

        return null; // Если не удалось найти допустимую позицию
    }


    // Проверяем, находится ли Pacman в пределах радиуса преследования
    private boolean isInRange() {
        int pacmanX = (int) (Pacman.x / blockSize);
        int pacmanY = (int) (Pacman.y / blockSize);
        int ghostX = (int) (x / blockSize);
        int ghostY = (int) (y / blockSize);

        // Рассчитываем евклидово расстояние между призраком и Pacman
        double distance = Math.sqrt(Math.pow(pacmanX - ghostX, 2) + Math.pow(pacmanY - ghostY, 2));
        return distance <= range;
    }

    // Случайное движение призрака
    private void makeRandomMove() {
        List<Node> neighbors = getNeighbors(new Node((int) (x / blockSize), (int) (y / blockSize), null, 0, 0));

        if (!neighbors.isEmpty()) {
            Node randomNeighbor = neighbors.get(random.nextInt(neighbors.size()));
            this.x = randomNeighbor.x * blockSize;
            this.y = randomNeighbor.y * blockSize;
        }
    }

    // Алгоритм A* для поиска пути
    private Node findPath(int startX, int startY, int goalX, int goalY) {
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        List<Node> closedList = new ArrayList<>();

        // Начальная точка
        Node startNode = new Node(startX, startY, null, 0, calculateHeuristic(startX, startY, goalX, goalY));
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();

            // Если нашли путь до Pacman
            if (currentNode.x == goalX && currentNode.y == goalY) {
                return reconstructPath(currentNode);
            }

            closedList.add(currentNode);

            // Проходим по соседям
            for (Node neighbor : getNeighbors(currentNode)) {
                if (closedList.contains(neighbor)) continue;

                int tentativeG = currentNode.g + 1; // Стоимость движения к соседу
                if (tentativeG < neighbor.g || !openList.contains(neighbor)) {
                    neighbor.g = tentativeG;
                    neighbor.f = neighbor.g + neighbor.h;
                    neighbor.parent = currentNode;

                    if (!openList.contains(neighbor)) {
                        openList.add(neighbor);
                    }
                }
            }
        }
        return null; // Путь не найден
    }

    // Получаем допустимых соседей
    private List<Node> getNeighbors(Node currentNode) {
        if (map == null || map.length == 0) {
            return new ArrayList<>(); // Возвращаем пустой список, чтобы избежать NPE
        }

        List<Node> neighbors = new ArrayList<>();
        int row = currentNode.y;
        int col = currentNode.x;

        // Проверь каждый сосед, чтобы избежать выхода за границы массива
        if (row > 0 && map[row - 1][col] != 1) {
            neighbors.add(new Node(col, row - 1, currentNode, 0, 0));
        }
        if (row < map.length - 1 && map[row + 1][col] != 1) {
            neighbors.add(new Node(col, row + 1, currentNode, 0, 0));
        }
        if (col > 0 && map[row][col - 1] != 1) {
            neighbors.add(new Node(col - 1, row, currentNode, 0, 0));
        }
        if (col < map[0].length - 1 && map[row][col + 1] != 1) {
            neighbors.add(new Node(col + 1, row, currentNode, 0, 0));
        }

        return neighbors;
    }

    // Вычисляем манхэттенское расстояние
    private int calculateHeuristic(int x, int y, int goalX, int goalY) {
        return Math.abs(x - goalX) + Math.abs(y - goalY);
    }

    // Восстанавливаем путь от целевого узла
    private Node reconstructPath(Node node) {
        Node current = node;
        while (current.parent != null && current.parent.parent != null) {
            current = current.parent; // Идем назад к первому шагу пути
        }
        return current;
    }



    // Отрисовка призрака
    public void draw(Canvas canvas, Paint paint) {
        Bitmap spriteToDraw = isVulnerable ? vulnerableSprite : ghostSprite;
        if (spriteToDraw != null) {
            canvas.drawBitmap(spriteToDraw, x, y, paint);
        }
    }

    // Проверка на столкновение с Pacman
    public boolean checkCollision(float pacmanX, float pacmanY) {
        if (Math.abs(pacmanX - x) < blockSize / 2 && Math.abs(pacmanY - y) < blockSize / 2) {
            if (isVulnerable) {
                GameView.score += 100;
                hideGhost();
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private void hideGhost() {
        this.x = 0;
        this.y = 0;
        this.setVulnerable(false);

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                respawn();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void respawn() {
        this.x = startX * blockSize;
        this.y = startY * blockSize;
        isVulnerable = false;
    }

    // Вспомогательный класс Node для A*
    private static class Node {
        int x, y;
        Node parent;
        int g, h, f;

        public Node(int x, int y, Node parent, int g, int h) {
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return x == node.x && y == node.y;
        }
    }
}
