"""Build two SVG plots from the measured CSV using only standard Python."""
import csv
import html
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
COLORS = ['#2563eb', '#e05232', '#15803d', '#9333ea', '#b77900', '#0891b2']


def plot(rows, x_field, label, filename):
    width, height = 1000, 540
    left, top, right, bottom = 90, 60, 720, 455
    algorithms = list(dict.fromkeys(row['algorithm'] for row in rows))
    xmax = max(int(row[x_field]) for row in rows) * 1.05
    ymax = max((float(row['average_time_ns']) + float(row['stddev_time_ns'])) / 1e6
               for row in rows) * 1.12
    ymax = max(ymax, 0.001)

    def x(value):
        return left + value / xmax * (right - left)

    def y(value):
        return bottom - value / ymax * (bottom - top)

    svg = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
           '<rect width="100%" height="100%" fill="white"/>',
           '<g font-family="Arial, sans-serif" font-size="14" fill="#17202a">',
           f'<text x="90" y="30" font-size="21">Execution time vs. {label.lower()}</text>']
    for i in range(6):
        value = ymax * i / 5
        yy = y(value)
        svg.append(f'<path d="M {left} {yy:.2f} H {right}" stroke="#e5e7eb"/>')
        svg.append(f'<text x="{left-12}" y="{yy+5:.2f}" text-anchor="end">{value:.3f}</text>')
    for value in sorted({int(row[x_field]) for row in rows}):
        xx = x(value)
        svg.append(f'<path d="M {xx:.2f} {bottom} v 5" stroke="#334155"/>')
        svg.append(f'<text x="{xx:.2f}" y="{bottom+25}" text-anchor="middle">{value}</text>')
    svg.append(f'<path d="M {left} {top} V {bottom} H {right}" fill="none" stroke="#334155"/>')
    svg.append(f'<text x="400" y="510" text-anchor="middle">{label}</text>')
    svg.append('<text x="22" y="260" text-anchor="middle" transform="rotate(-90 22 260)">Mean execution time (ms)</text>')
    for index, algorithm in enumerate(algorithms):
        color = COLORS[index % len(COLORS)]
        data = sorted((row for row in rows if row['algorithm'] == algorithm), key=lambda row: int(row[x_field]))
        points = ' '.join(f'{x(int(row[x_field])):.2f},{y(float(row["average_time_ns"])/1e6):.2f}' for row in data)
        svg.append(f'<polyline points="{points}" fill="none" stroke="{color}" stroke-width="2"/>')
        for row in data:
            xx = x(int(row[x_field]))
            mean = float(row['average_time_ns']) / 1e6
            sd = float(row['stddev_time_ns']) / 1e6
            high, low = y(mean + sd), y(max(0, mean - sd))
            svg.append(f'<path d="M {xx:.2f} {high:.2f} V {low:.2f} M {xx-4:.2f} {high:.2f} H {xx+4:.2f} M {xx-4:.2f} {low:.2f} H {xx+4:.2f}" stroke="{color}"/>')
            svg.append(f'<circle cx="{xx:.2f}" cy="{y(mean):.2f}" r="4" fill="{color}"/>')
        yy = 85 + index * 32
        svg.append(f'<path d="M 750 {yy} h 25" stroke="{color}" stroke-width="3"/>')
        svg.append(f'<text x="785" y="{yy+5}">{html.escape(algorithm)}</text>')
    svg.append('<text x="750" y="310" font-size="13">5 runs; bars: sample SD</text>')
    svg.append('<text x="750" y="335" font-size="13">V and E change together.</text>')
    svg.append('</g></svg>')
    output = ROOT / 'results' / 'plots' / filename
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text('\n'.join(svg), encoding='utf-8')


if __name__ == '__main__':
    with (ROOT / 'results/tables/summary.csv').open(newline='') as source:
        rows = list(csv.DictReader(source))
    plot(rows, 'vertices', 'Number of vertices', 'time-vs-vertices.svg')
    plot(rows, 'edges', 'Number of edges', 'time-vs-edges.svg')
    print('Saved two SVG plots in results/plots')
