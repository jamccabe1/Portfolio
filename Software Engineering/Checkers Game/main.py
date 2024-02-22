import pygame
import pygame_menu
from checkers.constants import WIDTH, HEIGHT, SQUARE_SIZE
from checkers.game import Game


def get_row_col_from_mouse(pos):
    x, y = pos
    row = y // SQUARE_SIZE
    col = x // SQUARE_SIZE
    return row, col

def main():
    run = True
    clock = pygame.time.Clock()
    game = Game(WIN)

    while run:
        clock.tick(FPS)

        if game.winner() != None:
            print(game.winner())
            run = False

        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                run = False
            
            if event.type == pygame.MOUSEBUTTONDOWN:
                pos = pygame.mouse.get_pos()
                row, col = get_row_col_from_mouse(pos)
                game.select(row, col)            

        game.update()
    
    pygame.quit()



FPS = 60
PLAYER1 = 'player1'
PLAYER2 = 'player2'
pygame.init()
WIN = pygame.display.set_mode((WIDTH, HEIGHT))
pygame.display.set_caption('Checkers')

menu = pygame_menu.Menu('Welcome to Checkers!', WIDTH, HEIGHT,
                       theme=pygame_menu.themes.THEME_DARK)

menu.add.text_input('Player 1:  ', default='player1')
menu.add.text_input('Player 2:  ', default='player2')
menu.add.button('Play', main)
menu.add.button('Quit', pygame_menu.events.EXIT)

menu.mainloop(WIN)