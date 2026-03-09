const { GoogleGenerativeAI } = require("@google/generative-ai");

// Initialize Gemini AI
const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

/**
 * Generate a detailed pathology report using Gemini AI
 * @param {Object} caseData - The case information
 * @param {string} caseData.patientName - Patient's name
 * @param {string} caseData.patientAge - Patient's age
 * @param {string} caseData.specimenSource - Source of the specimen (e.g., "Skin, left forearm, shave biopsy")
 * @param {string} caseData.aiPrediction - AI prediction result (e.g., "Malignant" or "Benign")
 * @param {number} caseData.confidence - Confidence percentage (e.g., 91.5)
 * @param {Array} caseData.findings - List of AI findings
 * @param {string} caseData.clinicalInfo - Clinical history or presentation
 * @returns {Promise<Object>} Generated pathology report with all sections
 */
async function generatePathologyReport(caseData) {
    try {
        const rawModelName = process.env.GEMINI_MODEL || "gemini-2.0-flash";
        const modelName = rawModelName.startsWith("models/")
            ? rawModelName.replace("models/", "")
            : rawModelName;
        console.log(`Gemini model in use: ${modelName}`);
        const model = genAI.getGenerativeModel({ model: modelName });

        const prompt = `You are an expert pathologist. Generate a comprehensive pathology report based on the following information:

Patient Information:
- Name: ${caseData.patientName}
- Age: ${caseData.patientAge || 'Not provided'}
- Specimen Source: ${caseData.specimenSource}

AI Analysis Results:
- Prediction: ${caseData.aiPrediction}
- Confidence: ${caseData.confidence}%
- Key Findings: ${caseData.findings.join(', ')}

Clinical Information:
${caseData.clinicalInfo || 'No clinical history provided.'}

Please generate a detailed pathology report with the following sections:

1. FINAL DIAGNOSIS: Provide a concise, definitive diagnosis in ALL CAPS. Be specific about the type and characteristics.

2. MICROSCOPIC DESCRIPTION: Describe the histopathological findings in detail, including:
   - Cellular architecture and organization
   - Presence of any abnormal cells or structures
   - Infiltration patterns if applicable
   - Presence of mitotic figures, necrosis, or other features
   - Any additional microscopic observations
   
3. MARGINS: Describe the status of surgical margins (involved, clear, close) and any relevant orientation information.

4. CLINICAL HISTORY: Provide a brief clinical context based on the patient presentation and specimen type. Include relevant details about symptoms, location, and associated conditions if applicable.

5. GROSS DESCRIPTION: Describe what the specimen would look like macroscopically, including:
   - Size (in cm)
   - Color and texture
   - Any visible lesions or abnormalities
   - How the specimen was processed for examination

Format your response as a JSON object with exactly these keys:
{
  "finalDiagnosis": "...",
  "microscopicDescription": "...",
  "margins": "...",
  "clinicalHistory": "...",
  "grossDescription": "..."
}

Make the report professional, medically accurate, and detailed. Use proper medical terminology.`;

        const result = await model.generateContent(prompt);
        const response = await result.response;
        const text = response.text();

        // Extract JSON from response (remove markdown formatting if present)
        let jsonText = text.trim();
        if (jsonText.startsWith('```json')) {
            jsonText = jsonText.replace(/```json\n?/g, '').replace(/```\n?/g, '');
        } else if (jsonText.startsWith('```')) {
            jsonText = jsonText.replace(/```\n?/g, '');
        }

        const report = JSON.parse(jsonText);

        // Validate that all required fields are present
        const requiredFields = ['finalDiagnosis', 'microscopicDescription', 'margins', 'clinicalHistory', 'grossDescription'];
        const missingFields = requiredFields.filter(field => !report[field]);
        
        if (missingFields.length > 0) {
            throw new Error(`Generated report is missing fields: ${missingFields.join(', ')}`);
        }

        return report;

    } catch (error) {
        console.error('Error generating pathology report:', error);
        
        // Return a fallback structured response if Gemini fails
        if (error.message && error.message.includes('API key')) {
            throw new Error('Invalid Gemini API key. Please check your configuration.');
        }
        
        throw new Error('Failed to generate pathology report: ' + error.message);
    }
}

module.exports = {
    generatePathologyReport,
    async listGeminiModels() {
        const response = await genAI.listModels();
        return response && response.models ? response.models : response;
    }
};
