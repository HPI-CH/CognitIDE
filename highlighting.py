import argparse
import json
from pathlib import Path


def main():
    # Parse arguments.
    parser = argparse.ArgumentParser()
    parser.add_argument("save_folder", type=Path)
    parser.add_argument("timestamp", type=float)
    args = parser.parse_args()

    # Read recording data.
    try:
        with open(args.save_folder / "sensorData.json", "r") as f:
            sensorData = json.load(f)
        with open(args.save_folder / "gazeData.json", "r") as f:
            gazeData = json.load(f)
    except FileNotFoundError:
        with open(args.save_folder / "highlighting.json", "w") as f:
            json.dump({}, f)
        exit()

    # Calculate highlighting intensities.
    highlighting = {}
    for (element_id, samples) in gazeData.items():
        highlighting[element_id] = 0
        for sample in samples:
            if args.timestamp != 0 and sample["time"] > args.timestamp:
                break
            highlighting[element_id] += sample["weight"]

    # Write calculation results to file.
    with open(args.save_folder / "highlighting.json", "w") as f:
        json.dump(highlighting, f)


if __name__ == "__main__":
    main()