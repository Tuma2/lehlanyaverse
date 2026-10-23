// Optimises the operator photos for the profile's Polaroid stack:
//   public/images/operator-<n>.webp + .jpg, 600x600, face-centred square crop, all metadata stripped
//
// Usage:  npm --prefix tools/og run photos -- <folder with the original photos>
// Originals stay out of the repo; only the optimised outputs are committed.
import sharp from "sharp";
import { mkdirSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(dirname(fileURLToPath(import.meta.url)), "../..");
const out = join(root, "site/src/jsMain/resources/public/images");
const srcDir = process.argv[2];
if (!srcDir) {
  console.error("Usage: npm --prefix tools/og run photos -- <folder with the original photos>");
  process.exit(1);
}

// Square crop per photo: centre (cx, cy) and side, as fractions of the source width/height.
// Tune these when a photo changes: the face should sit in the upper-middle of the square.
const photos = [
  { file: "portfolio3.jpg", name: "operator-1", cx: 0.5, cy: 0.5, side: 0.98 }, // studio portrait
  { file: "portfolio2.jpg", name: "operator-2", cx: 0.42, cy: 0.27, side: 0.82 }, // balcony
  { file: "portfolio1.jpg", name: "operator-3", cx: 0.5, cy: 0.4, side: 0.98 }, // beanie selfie
];
const SIZE = 600;

mkdirSync(out, { recursive: true });
for (const p of photos) {
  // rotate() applies EXIF orientation first, so the crop maths works on the image as it is seen
  const upright = await sharp(join(srcDir, p.file)).rotate().toBuffer({ resolveWithObject: true });
  const { width, height } = upright.info;
  const side = Math.round(Math.min(width, height) * p.side);
  const left = Math.min(Math.max(Math.round(width * p.cx - side / 2), 0), width - side);
  const top = Math.min(Math.max(Math.round(height * p.cy - side / 2), 0), height - side);
  const square = sharp(upright.data).extract({ left, top, width: side, height: side }).resize(SIZE, SIZE);

  // sharp writes no EXIF/GPS unless asked to (withMetadata), so phone location data never ships
  const webp = await square.clone().webp({ quality: 78 }).toFile(join(out, `${p.name}.webp`));
  const jpg = await square.clone().jpeg({ quality: 80, mozjpeg: true }).toFile(join(out, `${p.name}.jpg`));
  console.log(`${p.name}: webp ${(webp.size / 1024).toFixed(0)} KB, jpg ${(jpg.size / 1024).toFixed(0)} KB`);
}
