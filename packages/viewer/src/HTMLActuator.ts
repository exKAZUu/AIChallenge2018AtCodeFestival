import { Tile } from '../../game/src/Core/Tile';
import { Position } from '../../game/src/Core/types';

export class HTMLActuator {
  private readonly tileContainer: HTMLElement;

  constructor(tileContainer: HTMLElement) {
    this.tileContainer = tileContainer;
  }

  public actuate(grid: Tile[][]) {
    window.requestAnimationFrame(() => {
      this.clearContainer(this.tileContainer);

      grid.forEach(column => {
        column.forEach(cell => {
          if (cell) {
            this.addTile(cell);
          }
        });
      });
    });
  }

  public clearContainer(container: HTMLElement) {
    while (container.firstChild) {
      container.removeChild(container.firstChild);
    }
  }

  public addTile(tile: Tile) {
    if (!tile.value) {
      return;
    }

    const wrapper = document.createElement('div');
    const inner = document.createElement('div');
    const position = tile.previousPosition || { row: tile.row, col: tile.col };
    const positionClass = this.positionClass(position);

    // We can't use classlist because it somehow glitches when replacing classes
    const classes = ['tile', `tile-${tile.value}`, positionClass];

    if (tile.value > 2048) {
      classes.push('tile-super');
    }

    this.applyClasses(wrapper, classes);

    inner.classList.add('tile-inner');
    inner.textContent = tile.value.toString(10);

    if (tile.previousPosition) {
      // Make sure that the tile gets rendered in the previous position first
      window.requestAnimationFrame(() => {
        classes[2] = this.positionClass({ row: tile.row, col: tile.col });
        this.applyClasses(wrapper, classes); // Update the position
      });
    } else if (tile.mergedFrom) {
      classes.push('tile-merged');
      this.applyClasses(wrapper, classes);

      // Render the tiles that merged
      tile.mergedFrom.forEach(merged => {
        this.addTile(merged);
      });
    } else {
      classes.push('tile-new');
      this.applyClasses(wrapper, classes);
    }

    // Add the inner part of the tile to the wrapper
    wrapper.appendChild(inner);

    // Put the tile on the board
    this.tileContainer.appendChild(wrapper);
  }

  private positionClass(position: Position) {
    return `tile-position-${position.col + 1}-${position.row + 1}`;
  }

  private applyClasses(element: HTMLElement, classes: string[]) {
    element.setAttribute('class', classes.join(' '));
  }
}
