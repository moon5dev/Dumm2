using System.Text;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

class Program
{
    private static readonly string BaseUrl = "http://localhost:8080";
    private static readonly HttpClient HttpClient = new HttpClient();

    static async Task Main(string[] args)
    {
        Console.WriteLine("=== Label Printer Client ===");

        var pendingList = await GetPendingQueue();

        if (pendingList == null || pendingList.Count == 0)
        {
            Console.WriteLine("No pending print jobs.");
            return;
        }

        Console.WriteLine($"Found {pendingList.Count} pending job(s).");

        foreach (var job in pendingList)
        {
            await ProcessPrintJob(job);
        }

        Console.WriteLine("=== Done ===");
    }

    static async Task<List<JObject>?> GetPendingQueue()
    {
        try
        {
            var response = await HttpClient.GetStringAsync($"{BaseUrl}/api/print-queue/pending");
            var list = JsonConvert.DeserializeObject<List<JObject>>(response);
            return list;
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[ERROR] Failed to fetch pending queue: {ex.Message}");
            return null;
        }
    }

    static async Task ProcessPrintJob(JObject job)
    {
        var id = job["id"]?.ToString();
        var templateName = job["templateName"]?.ToString();
        var templateJson = job["templateJson"]?.ToString();
        var printData = job["printData"]?.ToString();
        var printerName = job["printerName"]?.ToString() ?? "DEFAULT";

        Console.WriteLine($"\n[JOB {id}] Template: {templateName} / Printer: {printerName}");

        Console.WriteLine($"[JOB {id}] templateJson: {templateJson}");
        Console.WriteLine($"[JOB {id}] printData: {printData}");

        try
        {
            var template = JObject.Parse(templateJson!);
            var dataMap = JsonConvert.DeserializeObject<Dictionary<string, string>>(printData!)
                            ?? new Dictionary<string, string>();

            var zpl = GenerateZpl(template, dataMap);

            Console.WriteLine("--- ZPL Output ---");
            Console.WriteLine(zpl);
            Console.WriteLine("------------------");

            SaveZplToFile(id!, zpl);

            await CallComplete(id!);
            Console.WriteLine($"[JOB {id}] Completed.");
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[JOB {id}] ERROR: {ex.Message}");
            await CallError(id!);
        }
    }

    static string GenerateZpl(JObject template, Dictionary<string, string> dataMap)
    {
        try
        {
            var sb = new StringBuilder();
            var size = template["size"];

            sb.AppendLine("^XA");

            var dpi = 203;
            var widthCm = size?["width"]?.Value<double?>() ?? 10.0;
            var heightCm = size?["height"]?.Value<double?>() ?? 5.0;
            var widthDot = CmToDots(widthCm, dpi);
            var heightDot = CmToDots(heightCm, dpi);

            sb.AppendLine($"^PW{widthDot}");
            sb.AppendLine($"^LL{heightDot}");

            var controls = template["controls"] as JArray;
            if (controls != null)
            {
                foreach (var ctrl in controls)
                {
                    var type = ctrl["type"]?.ToString();
                    var fieldType = ctrl["fieldType"]?.ToString() ?? "STATIC";
                    var value = ctrl["value"]?.ToString() ?? "";
                    var x = CmToDots(ctrl["x"]?.Value<double?>() ?? 0.0, dpi);
                    var y = CmToDots(ctrl["y"]?.Value<double?>() ?? 0.0, dpi);

                    if (fieldType != "STATIC" && dataMap.ContainsKey(fieldType))
                    {
                        value = dataMap[fieldType];
                    }

                    switch (type)
                    {
                        case "text":
                            var fs = ctrl["fontSize"]?.Value<int?>() ?? 12;
                            sb.AppendLine($"^FO{x},{y}");
                            sb.AppendLine($"^A0N,{fs},{fs}");
                            sb.AppendLine($"^FD{value}^FS");
                            break;

                        case "barcode":
                            var barcodeHeight = CmToDots(ctrl["height"]?.Value<double?>() ?? 1.0, dpi);
                            sb.AppendLine($"^FO{x},{y}");
                            sb.AppendLine($"^BCN,{barcodeHeight},Y,N,N");
                            sb.AppendLine($"^FD{value}^FS");
                            break;

                        case "qrcode":
                            sb.AppendLine($"^FO{x},{y}");
                            sb.AppendLine($"^BQN,2,5");
                            sb.AppendLine($"^FDQA,{value}^FS");
                            break;
                    }
                }
            }

            sb.AppendLine("^XZ");

            return sb.ToString();
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[ZPL ERROR] {ex.Message}");
            Console.WriteLine($"[ZPL ERROR] {ex.StackTrace}");
            throw;
        }
    }

    static void SaveZplToFile(string jobId, string zpl)
    {
        var dir = "output";
        Directory.CreateDirectory(dir);
        var path = Path.Combine(dir, $"job_{jobId}_{DateTime.Now:yyyyMMddHHmmss}.zpl");
        File.WriteAllText(path, zpl);
        Console.WriteLine($"[JOB {jobId}] ZPL saved to {path}");
    }

    static async Task CallComplete(string id)
    {
        var response = await HttpClient.PutAsync(
            $"{BaseUrl}/api/print-queue/{id}/complete",
            new StringContent("", Encoding.UTF8, "application/json")
        );
        if (!response.IsSuccessStatusCode)
            throw new Exception($"Complete API failed: {response.StatusCode}");
    }

    static async Task CallError(string id)
    {
        await HttpClient.PutAsync(
            $"{BaseUrl}/api/print-queue/{id}/error",
            new StringContent("", Encoding.UTF8, "application/json")
        );
    }

    static int CmToDots(double cm, int dpi)
    {
        return (int)Math.Round(cm * dpi / 2.54);
    }
}