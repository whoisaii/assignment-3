"""Replace only the measured-results block in README.md."""
import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
START = '<!-- RESULTS_START -->'
END = '<!-- RESULTS_END -->'


def build():
    with (ROOT / 'results/tables/summary.csv').open(newline='') as source:
        rows = list(csv.DictReader(source))
    lines = ['### Measured execution times', '',
             '| Algorithm | V | E | Mean (ms) | Sample SD (ms) |',
             '| --- | ---: | ---: | ---: | ---: |']
    for row in rows:
        lines.append(f'| {row["algorithm"]} | {row["vertices"]} | {row["edges"]} | '
                     f'{float(row["average_time_ns"])/1e6:.6f} | {float(row["stddev_time_ns"])/1e6:.6f} |')
    lines += ['', '### Operation counts per run', '',
              '| Algorithm | V | Edge checks | Comparisons | Find | Union | Parent hops | Offers | Polls |',
              '| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |']
    for row in rows:
        fields = ['algorithm', 'vertices', 'edge_checks', 'comparisons', 'find_calls',
                  'union_calls', 'parent_hops', 'queue_offers', 'queue_polls']
        lines.append('| ' + ' | '.join(row[field] for field in fields) + ' |')
    lines += ['', '### Recorded environment', '', '```text',
              (ROOT / 'results/tables/environment.txt').read_text().strip(), '```', '',
              '### Comparison of measured results', '']
    for v in [10, 100, 1000]:
        group = {row['algorithm']: row for row in rows if int(row['vertices']) == v}
        ratio = float(group['Kruskal']['average_time_ns']) / float(group['Prim']['average_time_ns'])
        winner = 'Kruskal' if ratio < 1 else 'Prim'
        lines.append(f'- V={v}: Kruskal/Prim mean-time ratio is {ratio:.2f}; {winner} is faster in this recorded run.')
    lines.append('')
    for algorithm in ['SCC', 'Dijkstra']:
        data = [row for row in rows if row['algorithm'] == algorithm]
        data.sort(key=lambda row: int(row['vertices']))
        ratio = float(data[-1]['average_time_ns']) / float(data[0]['average_time_ns'])
        lines.append(f'{algorithm}: large/small mean-time ratio is {ratio:.2f}. '
                     'This is an observation for these graphs and this JVM run, not an asymptotic bound.')
        lines.append('')
    readme = ROOT / 'README.md'
    text = readme.read_text()
    if text.count(START) != 1 or text.count(END) != 1:
        raise ValueError('README must contain exactly one results marker pair')
    before, rest = text.split(START)
    _, after = rest.split(END)
    readme.write_text(before + START + '\n\n' + '\n'.join(lines) + '\n\n' + END + after)
    print('Updated measured-results block in README.md')


if __name__ == '__main__':
    build()
