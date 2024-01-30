import numpy as np
import matplotlib.pyplot as plt

# Renklerin atanacağı sözlük
colors = {
    1: "red",
    2: "blue",
    3: "green",
    4: "yellow",
    5: "orange",
    6: "purple",
    7: "pink",
    8: "cyan",
    9: "magenta",
    10: "lime",
    11: "teal",
    12: "olive",
    13: "brown",
    14: "navy",
    15: "salmon",
    16: "gold",
}

def print_matrix(matrix):
    for row in matrix:
        print(" ".join(str(cell).zfill(2) if cell != 0 else "00" for cell in row))


def create_gcode(matrix, filename, space_height, space_width):
    cell_size = 10
    gcode = []

    def move_to(x, y, block_number):
        gcode.append("SU")
        gcode.append(f"G1 X{x} Y{y} ; {block_number}")
        gcode.append("SD")

    def draw_block(min_x, min_y, max_x, max_y, block_number):
        move_to(min_x, min_y, block_number)
        gcode.append(f"G1 X{max_x} Y{min_y}")
        gcode.append(f"G1 X{max_x} Y{max_y}")
        gcode.append(f"G1 X{min_x} Y{max_y}")
        gcode.append(f"G1 X{min_x} Y{min_y}")

    draw_block(0, 0, space_width * cell_size, space_height * cell_size, "0")
    connected_regions = find_connected_regions(matrix)
    for i, region in enumerate(connected_regions, start=1):
        min_x = min(x for x, _, _ in region) * cell_size
        min_y = min(y for _, y, _ in region) * cell_size
        max_x = (max(x for x, _, _ in region) + 1) * cell_size
        max_y = (max(y for _, y, _ in region) + 1) * cell_size
        draw_block(min_x, min_y, max_x, max_y, i)

    gcode.append("SU")
    gcode.append("G1 X0 Y0")
    with open(filename, "w") as gcode_file:
        for line in gcode:
            gcode_file.write(line + "\n")


def find_connected_regions(matrix):
    connected_regions = []
    visited = [[False] * len(matrix[0]) for _ in range(len(matrix))]

    def explore_connected_component(start_i, start_j, number):
        connected_component = []
        stack = [(start_i, start_j)]
        while stack:
            i, j = stack.pop()
            if (
                0 <= i < len(matrix)
                and 0 <= j < len(matrix[0])
                and matrix[i][j] == number
                and not visited[i][j]
            ):
                connected_component.append((j, i, number))
                visited[i][j] = True
                stack.append((i, j + 1))  # Move right
                stack.append((i + 1, j))  # Move down
        return connected_component

    for number in range(1, max(max(row) for row in matrix) + 1):
        for i in range(len(matrix)):
            for j in range(len(matrix[0])):
                if matrix[i][j] == number and not visited[i][j]:
                    connected_component = explore_connected_component(i, j, number)
                    connected_regions.append(connected_component)
    return connected_regions


def main():
    gcode_filename = "../gCodeSender/bestMatrix.gcode"

    # Metin dosyasını oku
    with open('bestMatrix.txt', 'r') as file:
        lines = file.readlines()
    data_list = [list(map(int, line.strip().replace(',', '').split())) for line in lines]
    numpy_array = np.array(data_list)
    space_height, space_width = numpy_array.shape

    create_gcode(numpy_array, gcode_filename, space_height, space_width)
    print(f"G-code saved as {gcode_filename}")

    image = np.zeros((numpy_array.shape[0], numpy_array.shape[1], 3))

    # Matrisi renklendirme
    for i in range(numpy_array.shape[0]):
        for j in range(numpy_array.shape[1]):
            color = colors.get(numpy_array[i][j], [1, 1, 1])  # Sayıya karşılık gelen renk
            image[i, j] = plt.cm.colors.to_rgb(color)

    # Görseli görüntüleme
    plt.imshow(image)
    plt.axis('off')  # Eksenleri kapat
    plt.show()


    plt.imshow(image)
    plt.savefig("img.png")
    plt.axis("off")
    plt.savefig("img.png", bbox_inches="tight")


if __name__ == "__main__":
    main()
