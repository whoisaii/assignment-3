#!/usr/bin/env python3
"""Rebuild portable SVG line plots from measured CSV data; Python standard library only."""
import csv
import html
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
rows = list(csv.DictReader((ROOT / 'results/tables/summary.csv').open()))
algorithms = list(dict.fromkeys(r['algorithm'] for r in rows))
colors = ['#2563eb', '#e05b22', '#00826a', '#7c3aed', '#b04487', '#475569']

def plot(field, label, name):
    width, height = 1040, 640
    left, right, top, bottom = 95, 770, 100, 535
    maximum = max(float(r['mean_ns']) + float(r['stddev_ns']) for r in rows) / 1e6 * 1.08
    xmax = max(int(r[field]) for r in rows)
    x = lambda v: left + int(v) / xmax * (right-left)
    y = lambda v: bottom - v / maximum * (bottom-top)
    parts = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
             '<rect width="100%" height="100%" fill="white"/>',
             '<g font-family="Arial, sans-serif" fill="#172337">',
             f'<text x="40" y="38" font-size="24" font-weight="bold">Execution time vs. {label.lower()}</text>',
             '<text x="40" y="66" font-size="14" fill="#526174">Mean of 5 runs; bars show sample standard deviation; 20 warmup runs per case</text>']
    for i in range(6):
        value = maximum*i/5
        parts += [f'<line x1="{left}" y1="{y(value):.2f}" x2="{right}" y2="{y(value):.2f}" stroke="#e2e8f0"/>',
                  f'<text x="{left-12}" y="{y(value)+5:.2f}" text-anchor="end" font-size="13">{value:.3f}</text>']
    for value in sorted({int(r[field]) for r in rows}):
        parts += [f'<line x1="{x(value):.2f}" y1="{top}" x2="{x(value):.2f}" y2="{bottom}" stroke="#edf0f4"/>',
                  f'<text x="{x(value):.2f}" y="{bottom+25}" text-anchor="middle" font-size="13">{value:,}</text>']
    parts += [f'<path d="M {left},{top} V {bottom} H {right}" fill="none" stroke="#64748b"/>',
              f'<text x="{(left+right)/2}" y="590" text-anchor="middle" font-size="16">{label} (linear scale)</text>',
              '<text transform="translate(24,320) rotate(-90)" text-anchor="middle" font-size="16">Execution time (ms)</text>']
    for i, algorithm in enumerate(algorithms):
        series = sorted((r for r in rows if r['algorithm']==algorithm), key=lambda r:int(r[field]))
        points = ' '.join(f"{x(r[field]):.2f},{y(float(r['mean_ns'])/1e6):.2f}" for r in series)
        color = colors[i]
        parts.append(f'<polyline points="{points}" fill="none" stroke="{color}" stroke-width="2"/>')
        for r in series:
            xx=x(r[field]);mean=float(r['mean_ns'])/1e6;sd=float(r['stddev_ns'])/1e6
            ya,yb=y(max(0,mean-sd)),y(mean+sd)
            parts += [f'<path d="M {xx:.2f},{ya:.2f} V {yb:.2f} M {xx-4:.2f},{ya:.2f} H {xx+4:.2f} M {xx-4:.2f},{yb:.2f} H {xx+4:.2f}" stroke="{color}"/>',
                      f'<circle cx="{xx:.2f}" cy="{y(mean):.2f}" r="4" fill="{color}"/>']
        yy=125+i*34
        parts += [f'<line x1="800" y1="{yy}" x2="825" y2="{yy}" stroke="{color}" stroke-width="3"/>',
                  f'<text x="835" y="{yy+5}" font-size="13">{html.escape(algorithm)}</text>']
    parts.append('<text x="40" y="625" font-size="12" fill="#526174">V and E grow together in the assigned configurations; these plots do not isolate their effects.</text></g></svg>')
    (ROOT/'results/plots'/name).write_text('\n'.join(parts))

plot('vertices','Number of vertices','time-vs-vertices.svg')
plot('edges','Number of edges','time-vs-edges.svg')
print('Created both plots from results/tables/summary.csv')
